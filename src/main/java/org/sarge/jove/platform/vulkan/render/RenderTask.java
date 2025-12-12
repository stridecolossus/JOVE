package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.*;
import java.util.function.IntFunction;
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
 * <li>Select the next in-flight frame instance to render</li>
 * <li>Acquire the framebuffer to be rendered from the swapchain</li>
 * <li>Compose the render sequence for the frame</li>
 * <li>Submit the render task</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * This implementation aims to fully utilise the multi-threaded nature of the hardware by rendering multiple <i>in flight</i> frames concurrently.
 * The number of in-flight frames can be overridden by the {@link #count(Swapchain)} method.
 * <p>
 * The swapchain and frame buffers are recreated on demand if the swapchain is {@link Invalidated}.
 * <p>
 * @author Sarge
 */
public class RenderTask implements Runnable, TransientObject {
	private final SwapchainManager manager;
	private final IntFunction<Framebuffer> factory;
	private final List<Framebuffer> framebuffers = new ArrayList<>();
	private final FrameComposer composer;
	private final FrameStateIterator iterator;

	/**
	 * Constructor.
	 * @param manager		Swapchain manager
	 * @param factory		Frame buffer factory
	 * @param composer		Composer for the render sequence
	 */
	public RenderTask(SwapchainManager manager, IntFunction<Framebuffer> factory, FrameComposer composer) {
		final var swapchain = manager.swapchain();
		this.manager = requireNonNull(manager);
		this.factory = requireNonNull(factory);
		this.composer = requireNonNull(composer);
		this.iterator = new FrameStateIterator(count(swapchain), swapchain.device());
		createFramebuffers(swapchain);
	}

	/**
	 * Cycle iterator for in-flight frames.
	 */
	private static class FrameStateIterator {
		private final List<FrameState> frames;
		private int index;

		/**
		 * Constructor.
		 * @param count			Number of in-flight frames
		 * @param device		Logical device
		 */
		public FrameStateIterator(int count, LogicalDevice device) {
			this.frames = IntStream
					.range(0, requireOneOrMore(count))
					.mapToObj(_ -> FrameState.create(device))
					.toList();
		}

		/**
		 * @return Next in-flight frame
		 */
		public FrameState next() {
			if(index == frames.size()) {
				index = 0;
			}
			return frames.get(index++);
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
	 * Determines the number of in-flight frames.
	 * By default this is the same as the {@link Swapchain#count()}.
	 * @param swapchain Swapchain
	 * @return Number of in-flight frames
	 */
	protected int count(Swapchain swapchain) {
		return swapchain.count();
	}

	/**
	 * Builds the framebuffers for the given swapchain.
	 */
	private void createFramebuffers(Swapchain swapchain) {
		assert framebuffers.isEmpty();
		final int count = swapchain.count();
		for(int n = 0; n < count; ++n) {
			framebuffers.add(factory.apply(n));
		}
	}

	/**
	 * Releases the framebuffers.
	 */
	private void releaseFramebuffers() {
		for(Framebuffer b : framebuffers) {
			b.destroy();
		}
		framebuffers.clear();
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

		// Acquire next frame buffer
		final Swapchain swapchain = manager.swapchain();
		final int index = frame.acquire(swapchain);
		final Framebuffer framebuffer = framebuffers.get(index);

		// Render frame
		final Buffer sequence = composer.compose(index, framebuffer);
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
		final LogicalDevice device = manager.swapchain().device();
		device.waitIdle();

		// Recreate the swapchain
		final Swapchain swapchain = manager.recreate();

		// Rebuild the framebuffers
		releaseFramebuffers();
		createFramebuffers(swapchain);
	}

	@Override
	public void destroy() {
		iterator.destroy();
		releaseFramebuffers();
	}
}
