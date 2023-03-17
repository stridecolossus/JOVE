package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>Vulkan frame</i> is used to acquire and present frames during the rendering process.
 * <p>
 * This is a blocking implementation that synchronises the acquire-render-present process as follows:
 * <ol>
 * <li>Wait for the previous frame to be rendered if {@link #acquire()} is invoked before rendering has completed</li>
 * <li>Wait for render task to be completed for the next frame</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * TODO - does this need to an abstraction? will we ever need alternative implementations?
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

	private final Semaphore available, ready;
	private final Fence fence;
	private int index;
	private boolean acquired;

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
		if(acquired) throw new IllegalStateException("Frame has already been acquired: " + this);
		acquired = true;

		// Wait for completion of the previous frame
		fence.waitReady();
		fence.reset();

		// Retrieve frame buffer index
		// TODO - swapchain returns a 'handle'? => store here, no need for parameter in present()
		index = swapchain.acquire(available, null);

		return index;
	}

	/**
	 * Submits the given render task and presents the completed frame.
	 * Blocks until the render task has been completed.
	 * @param render		Render task
	 * @param swapchain		Swapchain
	 * @throws IllegalStateException if this frame has not been acquired
	 */
	public void present(Command.Buffer render, Swapchain swapchain) {
		if(!acquired) throw new IllegalStateException("Frame has not been acquired: " + this);

		// Submit render task
		submit(render);

		// Wait for frame to be rendered
		fence.waitReady();

		// Present completed frame
		final WorkQueue queue = render.pool().queue();
		swapchain.present(queue, index, ready);
		acquired = false;
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
