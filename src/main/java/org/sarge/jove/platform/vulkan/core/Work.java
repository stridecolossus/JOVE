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
 * Batch batch = new Batch(List.of(work, ...));
 * batch.submit(fence);
 * </pre>
 * @see Command
 * @see Family
 * @author Sarge
 */
public interface Work {
	/**
	 * Submit this work for execution.
	 * @param fence Optional fence
	 */
	void submit(Fence fence);

	/**
	 * Helper - Creates a work submission for the given command buffer.
	 * @param buffer Command buffer
	 * @return New work
	 */
	static DefaultWork of(Buffer buffer) {
		final Pool pool = buffer.pool();
		return new Builder(pool).add(buffer).build();
	}

	/**
	 * Helper - Submits the given <i>one time</i> command to the given pool.
	 * @param cmd		Command
	 * @param pool		Pool
	 * @param fence		Optional fence
	 * @return Command buffer
	 * @see VkCommandBufferUsage#ONE_TIME_SUBMIT
	 */
	static Buffer submit(Command cmd, Pool pool, Fence fence) {
		// Allocate and record one-time command
		final Buffer buffer = pool
				.allocate()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
				.add(cmd)
				.end();

		// Submit work
		final Work work = Work.of(buffer);
		work.submit(fence);

		return buffer;
	}

	/**
	 * @return Whether the given command pools share the same queue family
	 */
	private static boolean matches(Pool left, Pool right) {
		final Family a = left.queue().family();
		final Family b = right.queue().family();
		return a.equals(b);
	}

	/**
	 * Default implementation.
	 */
	class DefaultWork implements Work {
		private final Pool pool;
		private final List<Buffer> buffers = new ArrayList<>();
		private final Map<Semaphore, Integer> wait = new LinkedHashMap<>();
		private final Set<Semaphore> signal = new HashSet<>();

		/**
		 * Constructor.
		 * @param pool Command pool
		 */
		private DefaultWork(Pool pool) {
			this.pool = notNull(pool);
		}

		/**
		 * @return Command pool for this work submission
		 */
		public Pool pool() {
			return pool;
		}

		/**
		 * @param info Submission descriptor for this work.
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

		@Override
		public void submit(Fence fence) {
			final Batch batch = new Batch(List.of(this));
			batch.submit(fence);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof DefaultWork that) &&
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
	}

	/**
	 * A <i>batch</i> is comprised of multiple work instances to be submitted in one operation.
	 */
	class Batch implements Work {
		private final List<DefaultWork> batch;
		private final Pool pool;

		/**
		 * Constructor.
		 * @param batch Work batch
		 * @throws IllegalArgumentException if the batch is empty or <b>all</b> work does not submit to the same queue family
		 */
		public Batch(List<DefaultWork> batch) {
			Check.notEmpty(batch);
			this.batch = List.copyOf(batch);
			this.pool = batch.get(0).pool();
			validate();
		}

		private void validate() {
			for(DefaultWork work : batch) {
				if(!Work.matches(pool, work.pool())) {
					throw new IllegalArgumentException(String.format("Work batch does not submit to the same queue family: pool=%s work=%s", pool, work));
				}
			}
		}

		@Override
		public void submit(Fence fence) {
			final VkSubmitInfo[] array = StructureHelper.array(batch, VkSubmitInfo::new, DefaultWork::populate);
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkQueueSubmit(pool.queue(), array.length, array, fence));
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Batch that) &&
					this.batch.equals(that.batch);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(pool)
					.append("batch", batch.size())
					.build();
		}
	}

	/**
	 * Builder for a work submission.
	 */
	class Builder {
		private final Command.Pool pool;
		private DefaultWork work;

		/**
		 * Constructor.
		 * @param pool Command pool for this work
		 */
		public Builder(Pool pool) {
			this.pool = notNull(pool);
			this.work = new DefaultWork(pool);
		}

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalStateException if the command buffer has not been recorded
		 * @throws IllegalArgumentException if any buffer does not match the queue family for this work
		 */
		public Builder add(Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

			// Check all work is submitted to the same queue family
			if(!Work.matches(pool, buffer.pool())) {
				throw new IllegalArgumentException("Command buffers must all submit to the same queue family: " + buffer);
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
		 * Adds a semaphore to wait on before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will wait
		 * @throws IllegalArgumentException if {@code stages} is empty
		 * @throws IllegalArgumentException for a duplicate semaphore
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
		public DefaultWork build() {
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
