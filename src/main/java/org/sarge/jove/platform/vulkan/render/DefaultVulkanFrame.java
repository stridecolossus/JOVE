package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * Default implementation with standard Vulkan synchronisation.
 * @author Sarge
 */
public class DefaultVulkanFrame implements VulkanFrame {
	/**
	 * Creates a frame instance for the given device.
	 * @param dev Logical device
	 */
	public static DefaultVulkanFrame create(LogicalDevice dev) {
		final var available = VulkanSemaphore.create(dev);
		final var ready = VulkanSemaphore.create(dev);
		final var fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		return new DefaultVulkanFrame(available, ready, fence);
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
	public DefaultVulkanFrame(VulkanSemaphore available, VulkanSemaphore ready, Fence fence) {
		if(available.equals(ready)) throw new IllegalArgumentException("Available and ready semaphores cannot be the same instance");
		this.available = requireNonNull(available);
		this.ready = requireNonNull(ready);
		this.fence = requireNonNull(fence);
	}

	@Override
	public int acquire(Swapchain swapchain) throws SwapchainInvalidated {
		// Wait for the previous frame to be completed
		fence.waitReady();

		// Acquire next buffer
		final int index = swapchain.acquire(available, null);

		// Ensure still waiting if the swapchain has been invalidated
		fence.reset();

		return index;
	}

	@Override
	public void present(Command.Buffer render, int index, Swapchain swapchain) {
		// Submit render task
		submit(render);
		fence.waitReady();

		// Present completed frame
		final WorkQueue queue = render.pool().queue();
		swapchain.present(queue, index, ready);
	}

	private void submit(Command.Buffer render) {
		new Work.Builder()
				.add(render)
        		.wait(available, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT))
        		.signal(ready)
        		.build()
        		.submit(fence);
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}
