package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.util.*;
import org.sarge.jove.util.NativeHelper.PointerToIntArray;
import org.sarge.lib.util.Check;

/**
 * A <i>work</i> instance represents a group of tasks to be submitted to a {@link WorkQueue}.
 * <p>
 * A work submission is comprised of:
 * <ul>
 * <li>the queue that performs the work</li>
 * <li>one-or-more command buffers</li>
 * <li>a set of <i>wait</i> semaphores specifying when the work can begin</li>
 * <li>a set of <i>signal</i> semaphores that are notified when <b>all</b> the buffers have been executed</li>
 * </ul>
 * <p>
 * Note that <b>all</b> command buffers in a work submission <b>must</b> be allocated from pools with the same queue family.
 * <p>
 * Usage:
 * <pre>
 * // Record a command buffer
 * Command.Buffer buffer = ...
 *
 * // Init synchronisation
 * Semaphore wait = ...
 * Semaphore signal = ...
 *
 * // Create work submission
 * Work work = new Builder()
 *     .add(buffer)
 *     .wait(wait, VkPipelineStage.TOP_OF_PIPE)
 *     .signal(signal)
 *     .build();
 *
 * // Submit work
 * Fence fence = ...
 * work.submit(fence);
 *
 * // Submit a batch of work
 * Work.submit(List.of(work, ...), fence);
 * </pre>
 * @see Command
 * @author Sarge
 */
public final class Work {
	/**
	 * Helper - Creates a new work submission for the given command buffer.
	 * @param buffer Command buffer
	 * @return Work
	 */
	public static Work of(Buffer buffer) {
		return Builder.of(buffer).build();
	}

	private final List<Buffer> buffers = new ArrayList<>();
	private final Map<Semaphore, Set<VkPipelineStage>> wait = new LinkedHashMap<>();
	private final Set<Semaphore> signal = new HashSet<>();

	private Work() {
	}

	/**
	 * @return Command pool for this submission
	 */
	public Pool pool() {
		return buffers.get(0).pool();
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
			final int[] stages = wait.values().stream().map(BitMask::new).mapToInt(BitMask::bits).toArray();
			info.pWaitDstStageMask = new PointerToIntArray(stages);
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
		// Check batch submits to same queue
		final Iterator<Work> work = batch.iterator();
		final Pool pool = work.next().pool();
		final WorkQueue queue = pool.queue();
		while(work.hasNext()) {
			final var family = work.next().pool().queue().family();
			if(!family.equals(queue.family())) {
				throw new IllegalArgumentException(String.format("Work batch does not submit to the same queue family: queue=%s work=%s", queue, work));
			}
		}

		// Submit batch
		final VkSubmitInfo[] array = StructureCollector.array(batch, new VkSubmitInfo(), Work::populate);
		final VulkanLibrary lib = pool.device().library();
		check(lib.vkQueueSubmit(queue, array.length, array, fence));
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Work that) &&
				this.pool().equals(that.pool()) &&
				this.buffers.equals(that.buffers) &&
				this.wait.equals(that.wait) &&
				this.signal.equals(that.signal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pool", pool())
				.append("buffers", buffers.size())
				.append("waits", wait.size())
				.append("signals", signal.size())
				.build();
	}

	/**
	 * Helper - Submits the given command as a {@link VkCommandBufferUsage#ONE_TIME_SUBMIT} primary command buffer and waits for completion.
	 * @param cmd		Command
	 * @param pool		Pool
	 * @return Allocated command buffer
	 * @see #submit(Buffer)
	 */
	public static Buffer submit(Command cmd, Pool pool) {
		final Buffer buffer = pool
				.primary()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
				.add(cmd)
				.end();

		submit(buffer);

		return buffer;
	}

	/**
	 * Helper - Submits the given command buffer and waits for completion.
	 * @param buffer Command buffer
	 * @see #submit(Command, Pool)
	 */
	public static void submit(Buffer buffer) {
		final DeviceContext dev = buffer.pool().device();
		final Fence fence = Fence.create(dev);
		try {
			Work.of(buffer).submit(fence);
			fence.waitReady();
		}
		finally {
			fence.destroy();
			buffer.free();
		}
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
			return new Builder().add(buffer);
		}

		private Work work = new Work();
		private WorkQueue queue;

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalStateException if the command buffer has not been recorded
		 * @throws IllegalArgumentException if {@link #buffer} does not match the queue family for this work
		 */
		public Builder add(Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) {
				throw new IllegalStateException("Command buffer has not been recorded: " + buffer);
			}

			// Check all work is submitted to the same queue family
			final WorkQueue that = buffer.pool().queue();
			if(queue == null) {
				queue = that;
			}
			else {
    			if(!queue.family().equals(that.family())) {
    				throw new IllegalArgumentException("Command buffer must submit to the queue family of this work: buffer=%s expected=%s".formatted(buffer, queue));
    			}
			}

			// Add buffer to this work
			work.buffers.add(buffer);

			return this;
		}

		/**
		 * Adds a wait semaphore that must be signalled before this batch can be executed.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will be signalled
		 * @throws IllegalArgumentException if {@code stages} is empty
		 * @throws IllegalArgumentException for a duplicate semaphore
		 */
		public Builder wait(Semaphore semaphore, Set<VkPipelineStage> stages) {
			Check.notNull(semaphore);
			Check.notEmpty(stages);
			if(work.wait.containsKey(semaphore)) {
				throw new IllegalArgumentException(String.format("Duplicate wait semaphore: %s (%s)", semaphore, stages));
			}
			work.wait.put(semaphore, Set.copyOf(stages));
			return this;
		}

		/**
		 * @see #wait(Semaphore, Collection)
		 */
		public Builder wait(Semaphore semaphore, VkPipelineStage... stages) {
			return wait(semaphore, Set.of(stages));
		}

		/**
		 * Adds a semaphore to be signalled when this batch has completed execution.
		 * @param semaphore Semaphore to be signalled
		 */
		public Builder signal(Semaphore semaphore) {
			Check.notNull(semaphore);
			work.signal.add(semaphore);
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 * @throws IllegalArgumentException if no command buffers have been added or any semaphore is used as both a wait and signal
		 */
		public Work build() {
			if(work.buffers.isEmpty()) {
				throw new IllegalArgumentException("No command buffers specified");
			}
			if(!Collections.disjoint(work.signal, work.wait.keySet())) {
				throw new IllegalArgumentException("Semaphore cannot be used as both wait and signal");
			}

			try {
				return work;
			}
			finally {
				work = null;
			}
		}
	}
}
