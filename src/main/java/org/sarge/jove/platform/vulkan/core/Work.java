package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.Command.Pool;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.lib.util.Check;

import com.sun.jna.Memory;

/**
 * The <i>work</i> class comprises a <i>batch</i> of commands to be submitted to a {@link Queue}.
 * @see Command
 * @author Sarge
 */
public class Work {
	/**
	 * Submits multiple work batches.
	 * @param work			Work batches
	 * @param fence			Optional fence
	 * @throws IllegalArgumentException if all batches do not have the same queue
	 * @throws VulkanException if the submit fails
	 */
	public static void submit(List<Work> work, Fence fence, VulkanLibrary lib) {
		// Determine submission queue and check all batches have the same queue
		Check.notEmpty(work);
		final Work first = work.get(0);
		final Queue queue = first.queue;
		if(!work.stream().map(e -> e.queue).allMatch(queue::equals)) throw new IllegalArgumentException("All work batches must submit to the same queue");

		// Convert descriptors to array
		// TODO - JNA marshals this correctly?
		final var array = work.stream().map(e -> e.info).toArray(VkSubmitInfo[]::new);

		// Submit work
//		final VulkanLibrary lib = dev.library();
		check(lib.vkQueueSubmit(queue.handle(), array.length, array, NativeObject.ofNullable(fence)));
	}

//	Each element of the pCommandBuffers member of each element of pSubmits must have been allocated from a VkCommandPool that was created for the same queue family queue belongs to

	/**
	 * Helper - Submits a <i>one time</i> command to the given pool and waits for completion.
	 * @param cmd		Command
	 * @param pool 		Command pool
	 */
	public static void submit(Command cmd, Pool pool) {
		// Allocate and record command
		final Buffer buffer = pool
				.allocate()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
				.add(cmd)
				.end();

		// Submit work and wait for completion
		try {
			final VulkanLibrary lib = pool.device().library();
			final Queue queue = pool.queue();
			new Builder(queue).add(buffer).build().submit(null, lib);
			queue.waitIdle(lib);
		}
		finally {
			buffer.free();
		}
	}

	private final VkSubmitInfo info;
	private final Queue queue;

	/**
	 * Constructor.
	 * @param info 			Descriptor for this work batch
	 * @param queue			Work queue
	 */
	Work(VkSubmitInfo info, Queue queue) {
		this.info = notNull(info);
		this.queue = notNull(queue);
	}

	/**
	 * @return Work queue for this batch
	 */
	public Queue queue() {
		return queue;
	}

	/**
	 * Submits this work batch.
	 * @param fence Optional fence
	 */
	public void submit(Fence fence, VulkanLibrary lib) {
		submit(List.of(this), fence, lib);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("queue", queue)
				.append("commands", info.commandBufferCount)
				.append("waits", info.waitSemaphoreCount)
				.append("signals", info.signalSemaphoreCount)
				.build();
	}

	/**
	 * Builder for a work batch.
	 */
	public static class Builder {
		private final Queue queue;
		private final List<Buffer> buffers = new ArrayList<>();
		private final Collection<Pair<Semaphore, Integer>> wait = new ArrayList<>();
		private final Set<Semaphore> signal = new HashSet<>();

		/**
		 * Constructor.
		 * @param queue Work queue for this submission
		 */
		public Builder(Queue queue) {
			this.queue = notNull(queue);
		}

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalStateException if the command buffer has not been recorded
		 * @throws IllegalArgumentException if any buffer does not match the queue family
		 */
		public Builder add(Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

			// Check all work is submitted to the same queue family
			if(!buffer.pool().queue().family().equals(queue.family())) {
				throw new IllegalArgumentException("Command buffers must all have the same queue: " + buffer);
			}

			// Add buffer to this work
			buffers.add(buffer);

			return this;
		}

		/**
		 * Adds a semaphore upon which to wait before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stages		Pipeline stage(s) at which this semaphore will occur
		 */
		public Builder wait(Semaphore semaphore, Set<VkPipelineStage> stages) {
			// TODO - not VK_PIPELINE_STAGE_HOST_BIT
			// TODO - check duplicates?
			Check.notNull(semaphore);
			Check.notEmpty(stages);
			final var entry = ImmutablePair.of(semaphore, IntegerEnumeration.mask(stages));
			wait.add(entry);
			return this;
		}

		/**
		 * Adds a semaphore upon which to wait before executing this batch.
		 * @param semaphore 	Wait semaphore
		 * @param stage			Pipeline stage at which this semaphore will occur
		 */
		public Builder wait(Semaphore semaphore, VkPipelineStage stage) {
			return wait(semaphore, Set.of(stage));
		}

		/**
		 * Adds a semaphore to be signalled when this batch has completed execution.
		 * @param semaphore Signal semaphore
		 */
		public Builder signal(Semaphore semaphore) {
			Check.notNull(semaphore);
			signal.add(semaphore);
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 * @throws IllegalArgumentException if the command buffers is empty
		 */
		public Work build() {
			// Validate
			if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");

			// Init batch descriptor
			final VkSubmitInfo info = new VkSubmitInfo();

			// Populate command buffers
			info.commandBufferCount = buffers.size();
			info.pCommandBuffers = Handle.toArray(buffers);

			if(!wait.isEmpty()) {
				// Populate wait semaphores
				final var semaphores = wait.stream().map(Pair::getLeft).collect(toList());
				info.waitSemaphoreCount = wait.size();
				info.pWaitSemaphores = Handle.toArray(semaphores);

				// Populate pipeline stage flags (which for some reason is a pointer to an integer array)
				final int[] stages = wait.stream().map(Pair::getRight).mapToInt(Integer::intValue).toArray();
				final Memory mem = new Memory(stages.length * Integer.BYTES);
				mem.write(0, stages, 0, stages.length);
				info.pWaitDstStageMask = mem;
			}

			// Populate signal semaphores
			info.signalSemaphoreCount = signal.size();
			info.pSignalSemaphores = Handle.toArray(signal);

			// Create work
			return new Work(info, queue);
		}
	}
}
