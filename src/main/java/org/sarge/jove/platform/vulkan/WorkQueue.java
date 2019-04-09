package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;

/**
 * A <i>work queue</i> is used to submit {@link Work}.
 * @author Sarge
 */
public class WorkQueue extends PointerHandle {
	/**
	 * Work submitted to this queue.
	 */
	public class Work {
		private final VkSubmitInfo info;
		private final Pointer fence;

		/**
		 * Constructor.
		 * @param queue		Work queue
		 * @param info		Descriptor
		 */
		private Work(VkSubmitInfo info, Pointer fence) {
			this.info = info;
			this.fence = fence;
		}

		/**
		 * Submits this work for execution.
		 */
		public void submit() {
			// TODO - multiple
			check(lib.vkQueueSubmit(WorkQueue.this.handle(), 1, new VkSubmitInfo[]{info}, fence));
		}
	}

	/**
	 * Builder for work to be submitted to this queue.
	 */
	public class WorkBuilder {
		private final Collection<Pointer> buffers = new ArrayList<>();
		private final Collection<Integer> waitStages = new StrictSet<>();
		private final List<Pointer> waitSemaphores = new ArrayList<>();
		private final List<Pointer> signalSemaphores = new ArrayList<>();
		private Pointer fence;

		private WorkBuilder() {
		}

		/**
		 * Adds a command buffer to submit.
		 * @param buffer Buffer to submit
		 * @throws IllegalArgumentException if the command buffer has not been recorded
		 */
		public WorkBuilder add(Command.Buffer buffer) {
			if(!buffer.isReady()) throw new IllegalArgumentException("Command buffer has not been recorded: " + buffer);
			buffers.add(buffer.handle());
			return this;
		}

		/**
		 * Adds a pipeline wait stage.
		 * @param stage Wait stage
		 * @throws IllegalArgumentException for a duplicate wait state
		 */
		public WorkBuilder wait(VkPipelineStageFlag stage) {
			waitStages.add(stage.value());
			return this;
		}

		/**
		 * Adds a semaphore to wait on.
		 * @param semaphore Wait semaphore
		 */
		public WorkBuilder wait(PointerHandle semaphore) {
			waitSemaphores.add(semaphore.handle());
			return this;
		}

		/**
		 * Adds a semaphore to be signalled after execution.
		 * @param semaphore Signal semaphore
		 */
		public WorkBuilder signal(PointerHandle semaphore) {
			signalSemaphores.add(semaphore.handle());
			return this;
		}

		/**
		 * Sets the fence for this work.
		 * @param fence Fence
		 */
		public WorkBuilder fence(Fence fence) {
			this.fence = fence.handle();
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 */
		public Work build() {
			// Init work descriptor
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
			return new Work(info, fence);
		}
	}

	private final VulkanLibraryLogicalDevice lib;

	/**
	 * Constructor.
	 * @param handle 	Handle
	 * @param lib		Vulkan API
	 */
	protected WorkQueue(Pointer handle, VulkanLibraryLogicalDevice lib) {
		super(handle);
		this.lib = notNull(lib);
	}

	/**
	 * Creates a work builder.
	 * @return New work builder
	 */
	public WorkBuilder work() {
		return new WorkBuilder();
	}

	/**
	 * Helper - Submits the given command buffer for execution.
	 * @param buffer Command buffer
	 */
	public void submit(Command.Buffer buffer) {
		new WorkBuilder().add(buffer).build().submit();
	}

	/**
	 * Waits for this queue to become idle.
	 */
	public void waitIdle() {
		check(lib.vkQueueWaitIdle(super.handle()));
	}
}
