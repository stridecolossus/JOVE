package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.lib.util.Check;

/**
 * The <i>frame presenter</i> is the controller for the process of rendering to the swapchain.
 * <p>
 * The presenter cycles through an array of <i>in flight</i> frames that are responsible for synchronisation of the render process.
 * The next in-flight {@link Frame} is selected using the {@link #next()} method.
 * <p>
 * @author Sarge
 */
public class FramePresenter implements TransientObject {
	private final Swapchain swapchain;
	private final FrameBuilder builder;
	private final Frame[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param swapchain		Swapchain
	 * @param builder		Render task builder
	 * @param frames		Number of in-flight frames
	 */
	public FramePresenter(Swapchain swapchain, FrameBuilder builder, int frames) {
		Check.oneOrMore(frames);
		this.swapchain = notNull(swapchain);
		this.builder = notNull(builder);
		this.frames = new Frame[frames];
		init();
	}

	/**
	 * Initialises the in-flight frames.
	 */
	private void init() {
		final DeviceContext dev = swapchain.device();
		Arrays.setAll(frames, n -> new Frame(dev));
	}

	/**
	 * Retrieves the next frame to be rendered.
	 * @return Next frame
	 */
	public synchronized Frame next() {
		final int index = next++ % frames.length;
		return frames[index];
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
				.build();
	}

	/**
	 * A <i>frame</i> encapsulates the process of rendering and presenting to the swapchain.
	 * <p>
	 * Generally the {@link #render(RenderSequence)} process is:
	 * <ol>
	 * <li>Acquire the next swapchain image</li>
	 * <li>Submit the render task</li>
	 * <li>Present the rendered frame to the swapchain</li>
	 * </ol>
	 * <p>
	 * Note that although the individual steps are asynchronous operations this is a blocking method.
	 * <p>
	 * This method blocks on the following conditions:
	 * <ul>
	 * <li>The previous work is completed</li>
	 * <li>The next swapchain image becomes available</li>
	 * <li>The render task has been completed and is ready for presentation</li>
	 * </ul>
	 * Note that this method does <b>not</b> block after the frame has been presented.
	 */
	public class Frame {
		private final Semaphore available, ready;
		private final Fence fence;

		private Frame(DeviceContext dev) {
			this.available = Semaphore.create(dev);
			this.ready = Semaphore.create(dev);
			this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		}

		/**
		 * Renders this frame.
		 * @param seq Render sequence
		 */
		public void render(RenderSequence seq) {
			// Wait for completion of the previous frame
			fence.waitReady();
			fence.reset();

			// Acquire next swapchain image
			final int index = swapchain.acquire(available, null);

			// Build render task
			final Buffer buffer = builder.build(index, seq);

			// Submit render task
			new Work.Builder(buffer.pool())
					.add(buffer)
					.wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
					.signal(ready)
					.build()
					.submit(fence);

			// Wait for frame to be rendered
			fence.waitReady();

			// Present rendered frame
			final Queue queue = buffer.pool().queue();
			swapchain.present(queue, index, ready);
		}

		/**
		 * Releases resources.
		 */
		private void destroy() {
			available.destroy();
			ready.destroy();
			fence.destroy();
		}
	}
}
