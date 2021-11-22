package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.StructureHelper;
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
 * 	// Record a command buffer
 * 	Command.Pool pool = ...
 * 	Command.Buffer buffer = ...
 *
 * 	// Init synchronisation
 * 	Semaphore wait = ...
 * 	Semaphore signal = ...
 * 	Fence fence = ...
 *
 *	// Create work submission
 * 	Work work = new Builder(pool)
 * 		.add(buffer)
 * 		.wait(wait, VkPipelineStage.TOP_OF_PIPE)
 * 		.signal(signal)
 * 		.build();
 *
 *	// Submit work
 * 	work.submit(null);
 *
 *	// Submit a work batch with a synchronisation fence
 * 	Work.submit(List.of(buffer, ...), fence);
 * </pre>
 * @see Command
 * @author Sarge
 */
public class Work {
	/**
	 * Helper - Creates a work submission for the given command buffer.
	 * @param buffer Command buffer
	 * @return New work
	 */
	public static Work of(Buffer buffer) {
		final Pool pool = buffer.pool();
		return new Builder(pool).add(buffer).build();
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
			info.pWaitDstStageMask = NativeObject.array(stages);
		}

		// Populate signal semaphores
		info.signalSemaphoreCount = signal.size();
		info.pSignalSemaphores = NativeObject.array(signal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pool", pool)
				.append("buffers", buffers.size())
				.append("waits", wait.size())
				.append("signals", signal.size())
				.build();
	}

	/**
	 * A <i>batch</i> is a convenience collection of work.
	 */
	public interface Batch {
		/**
		 * Submits this batch of work.
		 * @param fence Optional fence
		 * @throws IllegalArgumentException if all batches do not submit to the same queue family
		 */
		void submit(Fence fence);

		/**
		 * Creates a work batch.
		 * @param work Work
		 * @return New work batch
		 */
		static Batch of(List<Work> work) {
			return fence -> {
				// Validate
				// TODO - factor out?
				Check.notEmpty(work);
				final Pool pool = work.get(0).pool;
				if(!work.stream().allMatch(e -> matches(e, pool))) throw new IllegalArgumentException("All work batches must submit to the same queue");

				// Populate array of submission descriptors
				final VkSubmitInfo[] array = StructureHelper.array(work, VkSubmitInfo::new, Work::populate);

				// Submit work
				final VulkanLibrary lib = pool.device().library();
				check(lib.vkQueueSubmit(pool.queue(), array.length, array, fence));
			};
		}
	}

	/**
	 * @return Whether the work submission has the same queue family as the given pool
	 */
	private static boolean matches(Work work, Pool pool) {
		final Family left = work.pool.queue().family();
		final Family right = pool.queue().family();
		return left.equals(right);
	}

	/**
	 * Helper - Submits the given <i>one time</i> command to the given pool.
	 * @param cmd		Command
	 * @param pool		Pool
	 * @return New command buffer
	 * @see VkCommandBufferUsage#ONE_TIME_SUBMIT
	 */
	public static Buffer submit(Command cmd, Pool pool) {
		// Allocate and record one-time command
		final Buffer buffer = pool
				.allocate()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
				.add(cmd)
				.end();

		// Submit work
		final Work work = Work.of(buffer);
		Batch.of(List.of(work)).submit(null);

		return buffer;
	}

	/**
	 * Builder for a work submission.
	 */
	public static class Builder {
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
		 * @throws IllegalArgumentException if any buffer does not match the queue family for this work
		 */
		public Builder add(Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

			// Check all work is submitted to the same queue family
			if(!matches(work, buffer.pool())) {
				throw new IllegalArgumentException("Command buffers must all submit to the same queue family: " + buffer);
			}

			// Add buffer to this work
			work.buffers.add(buffer);

			return this;
		}

		// TODO - check not wait/signal same

		/**
		 * Adds a semaphore upon which to wait before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will occur
		 * @throws IllegalArgumentException if {@code stages} is empty
		 * @throws IllegalArgumentException for a duplicate semaphore
		 */
		public Builder wait(Semaphore semaphore, Collection<VkPipelineStage> stages) {
			Check.notNull(semaphore);
			Check.notEmpty(stages);
			if(work.wait.containsKey(semaphore)) throw new IllegalArgumentException(String.format("Duplicate wait semaphore: %s (%s)", semaphore, stages));
			work.wait.put(semaphore, IntegerEnumeration.mask(stages));
			return this;
		}

		/**
		 * Adds a semaphore upon which to wait before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will occur
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
			if(!Collections.disjoint(work.signal, work.wait.keySet())) throw new IllegalArgumentException("Semaphore cannot be used as wait and signal");

			try {
				return work;
			}
			finally {
				work = null;
			}
		}
	}
}
