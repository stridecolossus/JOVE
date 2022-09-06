package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A <i>work</i> instance represents a group of tasks to be submitted to a {@link Queue}.
 * <p>
 * A work submission is comprised of:
 * <ul>
 * <li>the queue to submit the work</li>
 * <li>one-or-more command buffers</li>
 * <li>a set of <i>wait</i> semaphores specifying when the work can begin</li>
 * <li>a set of <i>signal</i> semaphores that are signalled when <b>all</b> the buffers have been executed</li>
 * </ul>
 * <p>
 * Note that <b>all</b> command buffers in a work submission <b>must</b> have been allocated from a pool created for the same queue family.
 * <p>
 * Usage:
 * <pre>
 * // Record a command buffer
 * Command.Pool pool = ...
 * Command.Buffer buffer = ...
 *
 * // Init synchronisation
 * Semaphore wait = ...
 * Semaphore signal = ...
 * Fence fence = ...
 *
 * // Create work submission
 * Work work = new Builder(pool)
 *     .add(buffer)
 *     .wait(wait, VkPipelineStage.TOP_OF_PIPE)
 *     .signal(signal)
 *     .build();
 *
 * // Submit work
 * work.submit(fence);
 *
 * // Submit a batch of work
 * Work.submit(List.of(work, ...), fence);
 * </pre>
 * @see Command
 * @author Sarge
 */
public class Work {
	/**
	 * Helper - Creates a new work submission for the given command buffer.
	 * @param buffer Command buffer
	 * @return New work
	 */
	public static Work of(Buffer buffer) {
		return Builder.of(buffer).build();
	}

	private final Pool pool;
	private final List<Buffer> buffers = new ArrayList<>();
	private final Map<Semaphore, Integer> wait = new LinkedHashMap<>();
	private final Set<Semaphore> signal = new HashSet<>();

	/**
	 * Constructor.
	 * @param pool Command pool
	 */
	private Work(Pool pool) {
		this.pool = notNull(pool);
	}

	/**
	 * @return Command pool for this work submission
	 */
	public Pool pool() {
		return pool;
	}

	/**
	 * Populates the submission descriptor for this work.
	 */
	private void populate(VkSubmitInfo info) {
		// Populate command buffers
		info.commandBufferCount = buffers.size();
		info.pCommandBuffers = NativeObject.array(buffers);

		if(!wait.isEmpty()) {
			// Populate wait semaphores
			info.waitSemaphoreCount = wait.size();
			info.pWaitSemaphores = NativeObject.array(wait.keySet());

			// Populate pipeline stage flags (which for some reason is a pointer to an integer array)
			final int[] stages = wait.values().stream().mapToInt(Integer::intValue).toArray();
			info.pWaitDstStageMask = new IntegerArray(stages);
		}

		// Populate signal semaphores
		info.signalSemaphoreCount = signal.size();
		info.pSignalSemaphores = NativeObject.array(signal);
	}

	/**
	 * Submits this work for execution.
	 * @param fence Optional fence
	 * @see #submit(Collection, Fence)
	 */
	public void submit(Fence fence) {
		submit(List.of(this), fence);
	}

	/**
	 * Submits a batch of work for execution.
	 * @param batch		Work batch
	 * @param fence		Optional fence
	 * @throws IllegalArgumentException if the batch does not submit to the same queue family
	 * @throws NoSuchElementException if the batch is empty
	 */
	public static void submit(Collection<Work> batch, Fence fence) {
		// Check batch submits to expected queue
		final Pool pool = batch.iterator().next().pool();
		for(Work work : batch) {
			if(!matches(pool, work.pool())) {
				throw new IllegalArgumentException(String.format("Work batch does not submit to the same queue family: pool=%s work=%s", pool, work));
			}
		}
		// TODO - reduce?

		// Submit batch
		final VkSubmitInfo[] array = StructureHelper.array(batch, VkSubmitInfo::new, Work::populate);
		final VulkanLibrary lib = pool.device().library();
		check(lib.vkQueueSubmit(pool.queue(), array.length, array, fence));
	}

	/**
	 * @return Whether the given command pools share the same queue family
	 */
	private static boolean matches(Pool left, Pool right) {
		final Family a = left.queue().family();
		final Family b = right.queue().family();
		return a.equals(b);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Work that) &&
				this.pool.equals(that.pool) &&
				this.buffers.equals(that.buffers) &&
				this.wait.equals(that.wait) &&
				this.signal.equals(that.signal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pool", pool)
				.append("buffers", buffers.size())
				.append("wait", wait.size())
				.append("signal", signal.size())
				.build();
	}

	/**
	 * Builder for a work submission.
	 */
	public static class Builder {
		/**
		 * Helper - Creates a new work builder for the given command buffer.
		 * @param buffer Command buffer
		 * @return New work builder
		 */
		public static Builder of(Buffer buffer) {
			return new Builder(buffer.pool()).add(buffer);
		}

		private Work work;

		/**
		 * Constructor.
		 * @param pool Command pool for this work
		 */
		public Builder(Pool pool) {
			this.work = new Work(pool);
		}

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalStateException if the command buffer has not been recorded
		 * @throws IllegalArgumentException if {@link #buffer} does not match the queue family for this work
		 */
		public Builder add(Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

			// Check all work is submitted to the same queue family
			if(!matches(work.pool, buffer.pool())) {
				throw new IllegalArgumentException(String.format("Command buffer must submit to the queue family of this work: buffer=%s pool=%s", buffer, work.pool));
			}

			// Add buffer to this work
			work.buffers.add(buffer);

			return this;
		}

		/**
		 * Adds a semaphore to wait on before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will be signalled
		 * @throws IllegalArgumentException if {@code stages} is empty
		 * @throws IllegalArgumentException for a duplicate semaphore
		 */
		public Builder wait(Semaphore semaphore, Collection<VkPipelineStage> stages) {
			Check.notNull(semaphore);
			Check.notEmpty(stages);
			if(work.wait.containsKey(semaphore)) throw new IllegalArgumentException(String.format("Duplicate wait semaphore: %s (%s)", semaphore, stages));
			work.wait.put(semaphore, IntegerEnumeration.reduce(stages));
			return this;
		}

		/**
		 * @see #wait(Semaphore, Collection)
		 */
		public Builder wait(Semaphore semaphore, VkPipelineStage... stages) {
			return wait(semaphore, Arrays.asList(stages));
		}

		/**
		 * Adds a semaphore to be signalled when this batch has completed execution.
		 * @param semaphore Signal semaphore
		 */
		public Builder signal(Semaphore semaphore) {
			Check.notNull(semaphore);
			work.signal.add(semaphore);
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 * @throws IllegalArgumentException if the command buffers is empty or any semaphore is used as both a wait and signal
		 */
		public Work build() {
			if(work.buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
			if(!Collections.disjoint(work.signal, work.wait.keySet())) throw new IllegalArgumentException("Semaphore cannot be used as both wait and signal");

			try {
				return work;
			}
			finally {
				work = null;
			}
		}
	}
}
