package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.vulkan.Command.Buffer;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;

/**
 * A <i>work queue</i> is used to submit {@link Work}.
 * @author Sarge
 */
public class WorkQueue extends Handle {
	/**
	 * Work submitted to this queue.
	 * @see WorkQueue#submit(Work)
	 */
	public static interface Work {
		/**
		 * @return Work descriptor
		 */
		VkSubmitInfo descriptor();

		/**
		 * Builder for work to be submitted to a queue.
		 */
		public static class Builder {
			private final Collection<Pointer> buffers = new ArrayList<>();
			private final Collection<Integer> waitStages = new StrictSet<>();
			private final List<Pointer> waitSemaphores = new ArrayList<>();
			private final List<Pointer> signalSemaphores = new ArrayList<>();

			/**
			 * Adds a command buffer to submit.
			 * @param buffer Buffer to submit
			 * @throws IllegalArgumentException if the command buffer has not been recorded
			 */
			public Builder add(Buffer buffer) {
				if(!buffer.isReady()) throw new IllegalArgumentException("Command buffer has not been recorded: " + buffer);
				buffers.add(buffer.handle());
				return this;
			}

			/**
			 * Adds a pipeline wait stage.
			 * @param stage Wait stage
			 * @throws IllegalArgumentException for a duplicate wait state
			 */
			public Builder wait(VkPipelineStageFlag stage) {
				waitStages.add(stage.value());
				return this;
			}

			/**
			 * Adds a semaphore to wait on.
			 * @param semaphore Wait semaphore
			 */
			public Builder wait(Handle semaphore) {
				waitSemaphores.add(semaphore.handle());
				return this;
			}

			/**
			 * Adds a semaphore to be signalled after execution.
			 * @param semaphore Signal semaphore
			 */
			public Builder signal(Handle semaphore) {
				signalSemaphores.add(semaphore.handle());
				return this;
			}

			/**
			 * Constructs this submission.
			 * @return New submission
			 */
			public Work build() {
				// Init descriptor
				final VkSubmitInfo info = new VkSubmitInfo();

				// Add wait stages
				// TODO - a lot if buggering about
				info.pWaitDstStageMask = StructureHelper.integers(ArrayUtils.toPrimitive(waitStages.toArray(Integer[]::new)));

				// Add command buffers to submit
				if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
				info.commandBufferCount = buffers.size();
				info.pCommandBuffers = StructureHelper.pointers(buffers);

				// Add wait semaphores
				info.waitSemaphoreCount = waitSemaphores.size();
				info.pWaitSemaphores = StructureHelper.pointers(waitSemaphores);

				// Add signal semaphores
				info.signalSemaphoreCount = signalSemaphores.size();
				info.pSignalSemaphores = StructureHelper.pointers(signalSemaphores);

				// Create work
				return () -> info;
			}
		}
	}

	private final VulkanLibraryLogicalDevice lib;

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	protected WorkQueue(Pointer handle, VulkanLibraryLogicalDevice lib) {
		super(handle);
		this.lib = notNull(lib);
	}

	/**
	 * Submits work to this queue.
	 * @param work Work to be submitted
	 */
	public void submit(Work work) {
		submitLocal(work, null);
	}

	/**
	 * Submits work with a fence to this queue.
	 * @param work 		Work to be submitted
	 * @param fence		Fence
	 */
	public void submit(Work work, Fence fence) {
		submitLocal(work, fence.handle());
	}

	/**
	 * Submits work to this queue.
	 */
	private void submitLocal(Work work, Pointer fence) {
		// TODO - multiple
		check(lib.vkQueueSubmit(super.handle(), 1, new VkSubmitInfo[]{work.descriptor()}, fence));
	}

	/**
	 * Waits for this queue to become idle.
	 */
	public void waitIdle() {
		check(lib.vkQueueWaitIdle(super.handle()));
	}
}
