package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>work</i> instance represents a group of tasks to be submitted to a {@link WorkQueue}.
 * <p>
 * A work submission is comprised of:
 * <ul>
 * <li>one-or-more command buffers</li>
 * <li>a set of <i>wait</i> semaphores specifying when the work can begin</li>
 * <li>a set of <i>signal</i> semaphores that indicate when <b>all</b> buffers have been executed</li>
 * </ul>
 * <p>
 * Note that <b>all</b> command buffers in a work submission <b>must</b> be allocated from pools with the same queue family.
 * <p>
 * @see Command
 * @author Sarge
 */
public record Work(List<Buffer> buffers, Map<VulkanSemaphore, Set<VkPipelineStage>> waiting, Set<VulkanSemaphore> signal) {
	/**
	 * Constructor.
	 * @param buffers		Command buffers
	 * @param waiting		Table of wait semaphores and pipeline stage(s)
	 * @param signal		Semaphores to be signalled
	 * @throws NoSuchElementException if {@link #buffers} is empty
	 * @throws IllegalStateException if any buffer has not been recorded
	 * @throws IllegalStateException if any buffer submits to a different queue family
	 * @throws IllegalArgumentException if any semaphore is used as both a wait and a signal
	 */
	public Work {
		// Check all buffers have been recorded and submit to the same queue family
		validate(buffers);
		validate(buffers.stream());

		// Check semaphores
		if(!Collections.disjoint(signal, waiting.keySet())) {
			throw new IllegalArgumentException("Semaphores cannot be used as both a wait and a signal");
		}

		buffers = List.copyOf(buffers);
		waiting = Map.copyOf(waiting);
		signal = Set.copyOf(signal);
	}

	/**
	 * @throws IllegalStateException if any buffer has not been recorded
	 */
	private static void validate(List<Buffer> buffers) {
		for(Buffer b : buffers) {
			if(!b.isReady()) {
				throw new IllegalStateException("Buffer has not been recorded: " + b);
			}
		}
	}

	/**
	 * @return Distinct command pool
	 * @throws IllegalStateException unless all buffers submit to the same queue family
	 */
	private static Pool validate(Stream<Buffer> buffers) {
    	final List<Pool> pools = buffers
            	.map(Buffer::pool)
            	.distinct()
            	.toList();

    	if(pools.size() != 1) {
    		throw new IllegalStateException("Command buffers must submit to the same queue family: pools=" + pools);
    	}

    	return pools.getFirst();
	}

	/**
	 * @return Submission descriptor for this work
	 */
	private VkSubmitInfo build() {
		// Populate command buffers
		final var info = new VkSubmitInfo();
		info.commandBufferCount = buffers.size();
		info.pCommandBuffers = NativeObject.handles(buffers);

		// Create a temporary copy of the wait table entries so that they can be iterated consistently
		final var entries = waiting
				.entrySet()
				.stream()
				.toList();

		// Populate wait semaphores
		info.waitSemaphoreCount = entries.size();
		info.pWaitSemaphores = entries
				.stream()
				.map(Entry::getKey)
				.collect(collectingAndThen(toList(), NativeObject::handles));

		// Populate pipeline stage flags (for some reason this is a pointer to an int-array)
		info.pWaitDstStageMask = entries
				.stream()
				.map(Entry::getValue)
				.map(EnumMask::new)
				.mapToInt(EnumMask::bits)
				.toArray();

		// Populate signal semaphores
		info.signalSemaphoreCount = signal.size();
		info.pSignalSemaphores = NativeObject.handles(signal);

		return info;
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
	 * Submits a work batch for execution.
	 * @param batch		Work batch
	 * @param fence		Optional fence signalled once <b>all</b> submitted buffers have been executed
	 * @throws NoSuchElementException if the batch is empty
	 * @throws IllegalStateException unless batches submit to the same queue family
	 */
	public static void submit(List<Work> batch, Fence fence) {
		// Check all work in this batch submits to same queue
		final Stream<Buffer> buffers = batch
        		.stream()
        		.map(Work::buffers)
        		.flatMap(Collection::stream);

		final Pool pool = validate(buffers);

		// Build batch descriptors
		final VkSubmitInfo[] info = batch
				.stream()
				.map(Work::build)
				.toArray(VkSubmitInfo[]::new);

		// Submit batch
		final LogicalDevice.Library library = pool.device().library();
		library.vkQueueSubmit(pool.queue(), info.length, info, fence);
	}

	/**
	 * Helper.
	 * Submits the given command as a {@link VkCommandBufferUsage#ONE_TIME_SUBMIT} primary command buffer and blocks until completion.
	 * @param cmd		Command
	 * @param pool		Pool
	 * @return Allocated command buffer
	 * @see #submit(CommandBuffer)
	 */
	public static Buffer submit(Command cmd, Pool pool) {
		final Buffer buffer = pool
				.allocate(1, true)
				.getFirst()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
				.add(cmd)
				.end();

		submit(buffer);

		return buffer;
	}

	/**
	 * Helper.
	 * Submits the given command buffer and blocks until completion.
	 * @param buffer Command buffer
	 */
	public static void submit(Buffer buffer) {
		// Create bounding fence
		final LogicalDevice device = buffer.pool().device();
		final Fence fence = Fence.create(device);

		// Create work instance
		final Work work = new Builder().add(buffer).build();

		// Submit work and block
		try {
			work.submit(fence);
			fence.waitReady();
		}
		finally {
			fence.destroy();
		}
	}

	/**
	 * Builder for a work submission.
	 */
	public static class Builder {
		private final List<Buffer> buffers = new ArrayList<>();
		private final Map<VulkanSemaphore, Set<VkPipelineStage>> wait = new HashMap<>();
		private final Set<VulkanSemaphore> signal = new HashSet<>();

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 */
		public Builder add(Buffer buffer) {
			buffers.add(buffer);
			return this;
		}

		/**
		 * Adds a wait semaphore that must be signalled before this batch can be executed.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will be signalled
		 */
		public Builder wait(VulkanSemaphore semaphore, Set<VkPipelineStage> stages) {
			wait.put(semaphore, stages);
			return this;
		}

		/**
		 * Adds a semaphore to be signalled when this batch has completed execution.
		 * @param semaphore Semaphore to be signalled
		 */
		public Builder signal(VulkanSemaphore semaphore) {
			signal.add(semaphore);
			return this;
		}

		/**
		 * Constructs this work.
		 * @return Work submission
		 */
		public Work build() {
			return new Work(buffers, wait, signal);
		}
	}
}
