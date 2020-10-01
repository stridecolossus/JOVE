package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkPipelineStageFlag;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Queue;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.StructureHelper;

/**
 * The <i>work</i> class defines a task to be submitted to a {@link Queue}.
 * @author Sarge
 */
@FunctionalInterface
public interface Work {
	/**
	 * Submits this work to the given queue.
	 */
	void submit();

	/**
	 * Builder for work.
	 */
	public static class Builder {
		private final List<Handle> buffers = new ArrayList<>();
		private final Set<Handle> wait = new HashSet<>();
		private final Set<Handle> signal = new HashSet<>();
		private final Set<VkPipelineStageFlag> stages = new HashSet<>();
		private Queue queue;

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalArgumentException if all added command buffers do not share the same queue
		 */
		public Builder add(Command.Buffer buffer) {
			// Determine queue for this work
			if(buffers.isEmpty()) {
				queue = buffer.pool().queue();
			}
			else {
				if(queue != buffer.pool().queue()) {
					throw new IllegalArgumentException(String.format("Invalid queue for command buffer: queue=%s buffer=%s", queue, buffer));
				}
			}

			// Add buffer to this work
			buffers.add(buffer.handle());

			return this;
		}

		public Builder wait(Semaphore semaphore) {
			Check.notNull(semaphore);
			wait.add(semaphore.handle());
			return this;
		}

		public Builder signal(Semaphore semaphore) {
			Check.notNull(semaphore);
			signal.add(semaphore.handle());
			return this;
		}

		public Builder stage(VkPipelineStageFlag stage) {
			Check.notNull(stage);
			stages.add(stage);
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 * @throws IllegalArgumentException if no command buffers were added
		 */
		public Work build() {
			// Create submission descriptor
			if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
			final VkSubmitInfo info = new VkSubmitInfo();

			// Populate command buffers
			info.commandBufferCount = buffers.size();
			info.pCommandBuffers = Handle.memory(buffers.toArray(Handle[]::new));

			// Populate wait semaphores
			info.waitSemaphoreCount = wait.size();
			info.pWaitSemaphores = Handle.memory(wait.toArray(Handle[]::new));

			// Populate signal semaphores
			info.signalSemaphoreCount = signal.size();
			info.pSignalSemaphores = Handle.memory(signal.toArray(Handle[]::new));

			// TODO
			final int[] array = stages.stream().mapToInt(IntegerEnumeration::value).toArray();
			info.pWaitDstStageMask = StructureHelper.integers(array);

			// Create work
			return () -> {
				final VulkanLibrary lib = queue.device().library();
				check(lib.vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, null)); // TODO - fence
			};
		}
	}
}