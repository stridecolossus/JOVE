package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.List;
import java.util.stream.IntStream;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.present.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.Invalidated;

/**
 * The <i>render task</i> orchestrates the various components that collaborate to render and present a frame to the swapchain.
 * <p>
 * The rendering process is as follows:
 * <ol>
 * <li>Acquire the framebuffer to be rendered from the swapchain</li>
 * <li>Compose the render sequence for the frame</li>
 * <li>Submit the render task</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * This implementation aims to fully utilise the multi-threaded nature of the hardware by rendering multiple <i>in flight</i> frames concurrently.
 * @see Swapchain#frames()
 * <p>
 * The swapchain and frame buffers are recreated on demand if the swapchain is {@link Invalidated}.
 * <p>
 * @author Sarge
 */
public class RenderTask implements Runnable, TransientObject {
	private final SwapchainManager manager;
	private final Framebuffer.Factory factory;
	private final FrameComposer composer;
	private final FrameStateIterator iterator;

	/**
	 * Constructor.
	 * @param manager		Swapchain manager
	 * @param factory		Frame buffer factory
	 * @param composer		Composer for the render sequence
	 */
	public RenderTask(SwapchainManager manager, Framebuffer.Factory factory, FrameComposer composer) {
		final var swapchain = manager.swapchain();
		this.manager = requireNonNull(manager);
		this.factory = requireNonNull(factory);
		this.composer = requireNonNull(composer);
		this.iterator = new FrameStateIterator(swapchain);
		factory.build(swapchain);
	}

	/**
	 * Cycle iterator for in-flight frames.
	 */
	private class FrameStateIterator {
		private final List<FrameState> frames;
		private int index = -1;

		public FrameStateIterator(Swapchain swapchain) {
			final int number = requireOneOrMore(swapchain.frames());
			final LogicalDevice device = swapchain.device();

			this.frames = IntStream
					.range(0, number)
					.mapToObj(_ -> frame(device))
					.toList();
		}

		/**
		 * @return Next in-flight frame
		 */
		public synchronized FrameState next() {
			if(++index == frames.size()) {
				index = 0;
			}
			return frames.get(index);
		}

		/**
		 * Releases resources.
		 */
		public void destroy() {
			for(FrameState f : frames) {
				f.destroy();
			}
		}
	}

	/**
	 * Creates a frame instance.
	 * @param device Logical device
	 * @return New frame instance
	 */
	protected FrameState frame(LogicalDevice device) {
		return FrameState.create(device);
	}

	@Override
	public void run() {
		try {
			render();
		}
		catch(Invalidated e) {
			recreate();
		}
	}

	/**
	 * Renders the next frame.
	 */
	private void render() throws Invalidated {
		// Select the next in-flight frame
		final FrameState frame = iterator.next();

		// Acquire frame buffer
		final Swapchain swapchain = manager.swapchain();
		final int index = frame.acquire(swapchain);
		final Framebuffer framebuffer = factory.framebuffer(index);

		// Render frame
		final Buffer sequence = composer.compose(iterator.index, framebuffer);
		frame.render(sequence);
		// TODO - how/where to record 'latest' COMPLETED framebuffer index? i.e. only ready/valid after render completed (?)

		// Present frame
		frame.present(sequence, index, swapchain);
	}

	/**
	 * Recreates the swapchain and framebuffers.
	 */
	private void recreate() {
		// Wait for pending rendering tasks
		final LogicalDevice device = manager.device();
		device.waitIdle();

		// Recreate the swapchain
		final Swapchain swapchain = manager.recreate();

		// Rebuild the framebuffers
		factory.build(swapchain);
	}

	@Override
	public void destroy() {
		iterator.destroy();
	}
}
