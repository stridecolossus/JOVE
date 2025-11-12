package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * A <i>Vulkan frame</i> tracks the state of an in-flight frame during the acquire-render-present process.
 * @author Sarge
 */
public class VulkanFrame implements TransientObject {
	/**
	 * Creates a frame instance for the given device.
	 * @param dev Logical device
	 */
	public static VulkanFrame create(LogicalDevice dev) {
		final var available = VulkanSemaphore.create(dev);
		final var ready = VulkanSemaphore.create(dev);
		final var fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		return new VulkanFrame(available, ready, fence);
	}

	private final VulkanSemaphore available, ready;
	private final Fence fence;

	/**
	 * Constructor.
	 * @param available		Signals a frame is available for rendering
	 * @param ready			Signals a frame has been rendered and is ready for presentation
	 * @param fence			Synchronises execution of the rendering work
	 * @throws IllegalArgumentException if {@link #available} and {@link #ready} are the same semaphore
	 */
	VulkanFrame(VulkanSemaphore available, VulkanSemaphore ready, Fence fence) {
		if(available.equals(ready)) {
			throw new IllegalArgumentException("Available and ready semaphores cannot be the same");
		}
		this.available = requireNonNull(available);
		this.ready = requireNonNull(ready);
		this.fence = requireNonNull(fence);
	}

	/**
	 * Acquires the next frame buffer.
	 * @param swapchain Swapchain
	 * @return Frame buffer index
	 */
	public int acquire(Swapchain swapchain) throws SwapchainInvalidated {
		// Wait for the previous frame to be completed
		fence.waitReady();

		// Acquire next buffer
		final int index = swapchain.acquire(available, null);

		// Ensure still waiting if the swapchain has been invalidated
		fence.reset();

		return index;
	}

	/**
	 * Renders the next frame frame and blocks until completion.
	 * @param sequence Render sequence
	 */
	public void render(Command.Buffer sequence) {
		submit(sequence);
		fence.waitReady();
	}

	private void submit(Command.Buffer sequence) {
		new Work.Builder()
        		.add(sequence)
        		.wait(available, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT))
        		.signal(ready)
        		.build()
        		.submit(fence);
	}

	/**
	 * Presents a completed frame.
	 * @param sequence		Render sequence
	 * @param index			Frame buffer index
	 * @param swapchain		Swapchain
	 */
	public void present(Command.Buffer sequence, int index, Swapchain swapchain) {
		final WorkQueue queue = sequence.pool().queue();
		swapchain.present(queue, index, ready);
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}
