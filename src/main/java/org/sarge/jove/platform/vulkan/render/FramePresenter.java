package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * The <i>frame presenter</i> is the controller for the process of rendering frames to the swapchain.
 * TODO
 * @author Sarge
 */
public class FramePresenter implements TransientObject {
	private final Swapchain swapchain;
	private final FrameBuilder builder;
	private final Frame[] frames;
	private int active;

	/**
	 * Constructor.
	 * @param swapchain		Swapchain
	 * @param builder		Render task builder
	 * @param frames		Number of in-flight frames
	 */
	public FramePresenter(Swapchain swapchain, FrameBuilder builder, int frames) {
		this.swapchain = requireNonNull(swapchain);
		this.builder = requireNonNull(builder);
		this.frames = new Frame[frames];
		init();
	}

	/**
	 * Initialises the in-flight frames.
	 */
	private void init() {
		final DeviceContext dev = swapchain.device();
		Arrays.setAll(frames, n -> frame(n, dev));
	}

	/**
	 * Creates a new in-flight frame.
	 * @param index		Frame index
	 * @param dev		Logical device
	 * @return New frame
	 */
	protected Frame frame(int index, DeviceContext dev) {
		final Semaphore available = Semaphore.create(dev);
		final Semaphore ready = Semaphore.create(dev);
		final Fence fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		return new Frame(available, ready, fence);
	}

	/**
	 * Selects the next in-flight frame to be rendered.
	 * @return Next frame
	 */
	public synchronized Frame next() {
		if(active >= frames.length) {
			active = 0;
		}
		return frames[active++];
	}

	@Override
	public void destroy() {
		for(Frame frame : frames) {
			frame.destroy();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(swapchain)
				.append(builder)
				.append("frames", frames.length)
				.append("active", active)
				.build();
	}

	/**
	 * A <i>frame</i> encapsulates the process of rendering and presenting the next frame to the swapchain.
	 */
	public class Frame {
		private final Semaphore available, ready;
		private final Fence fence;

		/**
		 * Constructor.
		 * @param available			Signals this frame is available for rendering
		 * @param ready				Signals this frame has been rendered and is ready for presentation
		 * @param fence				Synchronises the render task
		 */
		protected Frame(Semaphore available, Semaphore ready, Fence fence) {
			this.available = requireNonNull(available);
			this.ready = requireNonNull(ready);
			this.fence = requireNonNull(fence);
		}

		/**
		 * Renders the next frame.
		 * @param seq Render sequence
		 */
		public void render(RenderSequence seq) {
			// Wait for previous in-flight frame to complete
			fence.waitReady();
			fence.reset();

			// Acquire next frame buffer
			final int index = swapchain.acquire(available, null);

			// Render frame
			final Buffer buffer = builder.build(index, seq);
			submit(buffer);

			// Block until frame rendered
			fence.waitReady();

			// Present rendered frame
			// TODO - present task builder?
			final Queue queue = buffer.pool().queue();
			swapchain.present(queue, index, ready);
		}

		/**
		 * Submits the render task for this frame.
		 * @param buffer Render task
		 */
		protected void submit(Buffer buffer) {
			new Work.Builder(buffer.pool())
					.add(buffer)
					.wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
					.signal(ready)
					.build()
					.submit(fence);
		}

		/**
		 * Releases synchronisation resources.
		 */
		protected void destroy() {
			available.destroy();
			ready.destroy();
			fence.destroy();
		}
	}
}
