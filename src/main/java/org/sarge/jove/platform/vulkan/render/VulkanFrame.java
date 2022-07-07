package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.IntFunction;

import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.scene.RenderTask;

/**
 * The <i>Vulkan frame</i> encapsulates the process of acquiring, rendering and presenting a frame.
 * <p>
 * This class manages the synchronisation of the various collaborators involved in rendering a frame:
 * <ul>
 * <li>the swapchain</li>
 * <li>a {@link FrameRenderer} that generates and submits a render sequence</li>
 * <li>and the presentation queue</li>
 * </ul>
 * <p>
 * A frame is comprised of the following synchronisation signals:
 * <ul>
 * <li>{@link #available()} signals when the frame has been acquired and is ready for rendering</li>
 * <li>{@link #ready()} signals the frame has been rendered and is ready for presentation</li>
 * <li>{@link #fence()} synchronises the rendering process of a given frame</li>
 * </ul>
 * <p>
 * The render process for a frame is:
 * <ol>
 * <li>Acquire the next swapchain image</li>
 * <li>Wait for the previous frame to complete (if still in progress)</li>
 * <li>Render the frame</li>
 * <li>Present the rendered frame</li>
 * </ol>
 * <p>
 * Note that the acquire, render and present steps are asynchronous operations.
 * <p>
 * @author Sarge
 */
public class VulkanFrame implements RenderTask.Frame {
	/**
	 * The <i>frame renderer</i> is responsible for rendering a frame.
	 */
	public interface FrameRenderer {
		/**
		 * Renders a frame.
		 * @param frame In-flight frame
		 */
		void render(VulkanFrame frame);
	}

	// Presentation
	private final Swapchain swapchain;
	private final Queue presentation;
	private final IntFunction<FrameRenderer> factory;

	// Synchronisation
	private final Semaphore available, ready;
	private final Fence fence;

	/**
	 * Constructor.
	 * @param swapchain				Swapchain
	 * @param presentation			Presentation queue
	 * @param factory				Frame renderer factory
	 */
	public VulkanFrame(Swapchain swapchain, Queue presentation, IntFunction<FrameRenderer> factory) {
		final DeviceContext dev = swapchain.device();
		this.swapchain = notNull(swapchain);
		this.presentation = notNull(presentation);
		this.factory = notNull(factory);
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
		// TODO - this still sucks? active[] is property of swapchain but never used by it, also requires null check in waitReady(), should be handled here?
		swapchain.waitReady(index, fence);
		fence.reset();

		// Render frame
		final FrameRenderer renderer = factory.apply(index);
		renderer.render(this);

		// Present frame
		swapchain.present(presentation, index, ready);
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}

