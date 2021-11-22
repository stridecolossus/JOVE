package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.scene.RenderTask;

/**
 * The <i>Vulkan frame</i> encapsulates the process of acquiring, rendering and presenting a frame.
 * TODO - sync
 * @author Sarge
 */
public class VulkanFrame implements RenderTask.Frame {
	/**
	 * The <i>renderer</i> is responsible for rendering a frame.
	 */
	public interface Renderer {
		/**
		 * Renders the next frame.
		 * @param index Swapchain image index
		 * @param frame Frame
		 */
		void render(int index, VulkanFrame frame);
	}

	// Presentation
	private final Swapchain swapchain;
	private final Queue presentation;
	private final Renderer renderer;

	// Synchronisation
	private final Semaphore available, ready;
	private final Fence fence;

	/**
	 * Constructor.
	 * @param swapchain				Swapchain
	 * @param presentation			Presentation queue
	 * @param renderer				Frame renderer
	 */
	public VulkanFrame(Swapchain swapchain, Queue presentation, Renderer renderer) {
		final DeviceContext dev = swapchain.device();
		this.swapchain = notNull(swapchain);
		this.presentation = notNull(presentation);
		this.renderer = notNull(renderer);
		this.available = Semaphore.create(dev);
		this.ready = Semaphore.create(dev);
		this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
	}

	/**
	 * @return Signalled when the acquired swapchain image is available
	 */
	public Semaphore available() {
		return available;
	}

	/**
	 * @return Signalled when this frame has been rendered and is ready for presentation
	 */
	public Semaphore ready() {
		return ready;
	}

	/**
	 * @return Synchronisation fence for rendering
	 */
	public Fence fence() {
		return fence;
	}

	@Override
	public void render() {
		// Wait for any previous work to complete
		fence.waitReady();

		// Retrieve next swapchain image index
		final int index = swapchain.acquire(available, null);

		// Wait for previous frame to complete
		swapchain.waitReady(index, fence);

		// Render frame
		fence.reset();
		renderer.render(index, this);

		// Present frame
		swapchain.present(presentation, index, Set.of(ready));
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}

