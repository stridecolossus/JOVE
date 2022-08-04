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
 * <p>
 * The presenter cycles through an array of <i>in flight</i> frames that are responsible for synchronisation of the render process.
 * The next in-flight {@link Frame} is selected using the {@link #next()} accessor.
 * <p>
 * The {@link DefaultFrame} is a default implementation of the standard approach for rendering a frame with Vulkan synchronisation primitives.
 * The {@link #frame(int, DeviceContext)} factory method can be over-ridden to change the frame implementation.
 * <p>
 * @see FrameBuilder
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
		return new DefaultFrame(available, ready, fence);
	}

	/**
	 * Selects the next in-flight frame to be rendered.
	 * @return Next frame
	 */
	protected synchronized Frame next() {
		if(active >= frames.length) {
			active = 0;
		}
		return frames[active++];
	}

	/**
	 * Renders the next frame.
	 * This is a blocking operation.
	 * @param seq Render sequence
	 * @see Frame
	 */
	public void render(RenderSequence seq) {
		final Frame frame = next();
		frame.render(seq, builder, swapchain);
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
	public interface Frame extends TransientObject {
		/**
		 * Renders this frame.
		 * @param seq				Render sequence
		 * @param builder			Task builder
		 * @param swapchain			Target swapchain
		 */
		void render(RenderSequence seq, FrameBuilder builder, Swapchain swapchain);
	}

	/**
	 * Default implementation for the <i>standard</i> frame rendering approach.
	 * <p>
	 * This implementation comprises the following steps:
	 * <ol>
	 * <li>Acquire the next frame buffer index from the swapchain</li>
	 * <li>Construct a task to render to this frame buffer via {@link FrameBuilder#build(int, RenderSequence)}</li>
	 * <li>Submit the render task to the rendering work queue</li>
	 * <li>Present the rendered frame to the swapchain</li>
	 * </ul>
	 * Note that currently this is a blocking implementation using Vulkan synchronisation primitives to manage the multi-threaded nature of the rendering process.
	 * <p>
	 * The render process blocks the current thread:
	 * <ul>
	 * <li>at the beginning of the task to ensure the previous in-flight has completed (avoiding out-of-order frames)</li>
	 * <li>after a render task has been submitted until the frame is ready for presentation</li>
	 * <ul>
	 * <p>
	 * This implementation assumes the application is responsible for invoking rendering tasks and task synchronisation, e.g. possibly using a scheduling task executor.
	 * <p>
	 */
	public static class DefaultFrame implements Frame {
		private final Semaphore available, ready;
		private final Fence fence;

		/**
		 * Constructor.
		 * @param available			Frame is available for rendering
		 * @param ready				Frame has been rendered and is ready for presentation
		 * @param fence				Render task synchronisation
		 */
		protected DefaultFrame(Semaphore available, Semaphore ready, Fence fence) {
			this.available = requireNonNull(available);
			this.ready = requireNonNull(ready);
			this.fence = requireNonNull(fence);
		}

		@Override
		public void render(RenderSequence seq, FrameBuilder builder, Swapchain swapchain) {
			// Wait for previous in-flight frame to complete
			fence.waitReady();
			fence.reset();

			// TODO - out-of-order check
			// prev = new Fence[swapchain.images];
			// prev[active].waitReady() - if not NULL!
			// prev[active] = this;
			// invalidates the above? check tutorial

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

		@Override
		public void destroy() {
			available.destroy();
			ready.destroy();
			fence.destroy();
		}
	}
}
