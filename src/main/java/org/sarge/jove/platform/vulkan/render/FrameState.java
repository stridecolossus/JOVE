package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.Swapchain.Invalidated;

/**
 * The <i>frame state</i> manages the synchronisation state of an <i>in-flight</i> frame during rendering.
 * @author Sarge
 */
public class FrameState implements TransientObject {
	/**
	 * Creates a frame state instance.
	 * @param device Logical device
	 */
	public static FrameState create(LogicalDevice device) {
		final var available = VulkanSemaphore.create(device);
		final var ready = VulkanSemaphore.create(device);
		final var fence = Fence.create(device, VkFenceCreateFlags.SIGNALED);
		return new FrameState(available, ready, fence);
	}

	private final VulkanSemaphore available, ready;
	private final Fence fence;

	/**
	 * Constructor.
	 * @param available		Signals the frame is available for rendering
	 * @param ready			Signals the frame has been rendered and is ready for presentation
	 * @param fence			Synchronises the rendering work
	 * @throws IllegalArgumentException if {@link #available} and {@link #ready} are the same semaphore
	 */
	FrameState(VulkanSemaphore available, VulkanSemaphore ready, Fence fence) {
		if(available.equals(ready)) {
			throw new IllegalArgumentException("Available and ready semaphores cannot be the same");
		}
		this.available = requireNonNull(available);
		this.ready = requireNonNull(ready);
		this.fence = requireNonNull(fence);
	}

	/**
	 * Acquires the index of the next frame buffer.
	 * @param swapchain Swapchain
	 * @return Frame buffer index
	 * @throws Invalidated if the frame buffer index cannot be acquired
	 */
	public int acquire(Swapchain swapchain) throws Invalidated {
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
	 * @see #submit(Buffer)
	 */
	public void render(Buffer sequence) {
		submit(sequence);
		fence.waitReady();
	}

	/**
	 * Submits the render sequence.
	 * @param sequence Render sequence
	 * @see #stages()
	 */
	protected void submit(Buffer sequence) {
		new Work.Builder()
        		.add(sequence)
        		.wait(available, stages())
        		.signal(ready)
        		.build()
        		.submit(fence);
	}

	/**
	 * @return Waiting pipeline stages for the render task
	 */
	protected Set<VkPipelineStageFlags> stages() {
		return Set.of(VkPipelineStageFlags.COLOR_ATTACHMENT_OUTPUT);
	}

	/**
	 * Presents a completed frame.
	 * @param sequence		Rendering sequence
	 * @param index			Frame buffer index
	 * @param swapchain		Swapchain
	 * @throws Invalidated if the frame cannot be presented
	 */
	public void present(Buffer sequence, int index, Swapchain swapchain) throws Invalidated {
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
