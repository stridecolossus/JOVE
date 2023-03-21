package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.lib.util.Check;

/**
 * A <i>Vulkan frame</i> tracks the state of an in-flight frame during the rendering process.
 * <p>
 * This is a blocking implementation that synchronises the acquire-render-present process as follows:
 * <ol>
 * <li>Wait for the previous frame to be rendered if {@link #acquire()} is invoked before rendering has completed</li>
 * <li>Wait for render task to be completed for the next frame</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * @author Sarge
 */
public class VulkanFrame implements TransientObject {
	/**
	 * Creates a frame instance for the given swapchain.
	 * @param swapchain Swapchain
	 */
	public static VulkanFrame create(DeviceContext dev) {
		final var available = Semaphore.create(dev);
		final var ready = Semaphore.create(dev);
		final var fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		return new VulkanFrame(available, ready, fence);
	}

	/**
	 * Creates an array of frames.
	 * @param dev		Logical device
	 * @param count		Number of frames
	 * @return Array of frames
	 */
	public static VulkanFrame[] array(DeviceContext dev, int count) {
		final var frames = new VulkanFrame[count];
		Arrays.setAll(frames, n -> VulkanFrame.create(dev));
		return frames;
	}

	private final Semaphore available, ready;
	private final Fence fence;
	private int index;
	private Swapchain swapchain;

	/**
	 * Constructor.
	 * @param available		Signals a frame is available for rendering
	 * @param ready			Signals a frame has been rendered and is ready for presentation
	 * @param fence			Synchronises execution of the rendering work
	 * @throws IllegalArgumentException if {@link #available} and {@link #ready} are the same semaphore
	 */
	public VulkanFrame(Semaphore available, Semaphore ready, Fence fence) {
		if(available.equals(ready)) throw new IllegalArgumentException("Available and ready semaphores cannot be the same instance");
		this.available = notNull(available);
		this.ready = notNull(ready);
		this.fence = notNull(fence);
	}

	/**
	 * Acquires the next frame to be rendered.
	 * Blocks until the previous frame has been rendered (if still running).
	 * @param swapchain Swapchain
	 * @return Frame index
	 * @throws IllegalStateException if this frame has already been acquired
	 */
	public int acquire(Swapchain swapchain) {
		// Acquire frame
		Check.notNull(swapchain);
		if(this.swapchain != null) throw new IllegalStateException("Frame has already been acquired: " + this);
		// TODO - do we need additional state and/or sync locks here?
		// i.e. state = pending | acquired | rendering | presented (panding?)
		// reentrant lock on acquire() and release when presented?

		// Wait for completion of the previous frame
		fence.waitReady();
		fence.reset();

		// Retrieve frame buffer index
		this.swapchain = swapchain;
		index = swapchain.acquire(available, null);

		return index;
	}

	/**
	 * Submits the given render task and presents the completed frame.
	 * Blocks until the render task has been completed.
	 * @param render Render task
	 * @throws IllegalStateException if this frame has not been acquired
	 */
	public void present(Command.Buffer render) {
		// Submit render task
		if(swapchain == null) throw new IllegalStateException("Frame has not been rendered: " + this);
		submit(render);

		// Wait for render to be completed
		fence.waitReady();

		// Present completed frame
		final WorkQueue queue = render.pool().queue();
		swapchain.present(queue, index, ready);
		swapchain = null;
	}

	/**
	 * Submits the render task.
	 * @param render Rendering work
	 */
	private void submit(Command.Buffer render) {
		new Work.Builder()
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
