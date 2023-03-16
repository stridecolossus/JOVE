package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;

/**
 *
 * @author Sarge
 */
public class VulkanFrame implements TransientObject {
	private final Semaphore available, ready;
	private final Fence fence;

	/**
	 * Constructor.
	 * @param dev Logical device
	 */
	public VulkanFrame(DeviceContext dev) {
		this.available = Semaphore.create(dev);
		this.ready = Semaphore.create(dev);
		this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
	}

	public Semaphore available() {
		return available;
	}

	public Semaphore ready() {
		return ready;
	}

	public Fence fence() {
		return fence;
	}

	/**
	 * Renders a frame.
	 * @param composer		Composes the render task for a given frame index
	 * @param swapchain		Swapchain
	 */
	void render(FrameComposer composer, Swapchain swapchain) {
		// Wait for completion of the previous frame
		fence.waitReady();
		fence.reset();

		// Acquire next swapchain image
		final int index = swapchain.acquire(available, null);

		// Build task for this frame
		final Command.Buffer render = composer.compose(index);

		// Submit render task
		submit(render);

		// Wait for frame to be rendered
		fence.waitReady();

		// Present rendered frame
		final WorkQueue queue = render.pool().queue();
		swapchain.present(queue, index, ready);
	}

	/**
	 * Submits a render task.
	 * @param render Render sequence
	 */
	private void submit(Command.Buffer render) {
		final Command.Pool pool = render.pool();
		new Work.Builder(pool)
				.add(render)
				.wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
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
