package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * The <i>render task</i> renders and presents a frame to the swapchain.
 * <p>
 * This class orchestrates the various components that collaborate to render a frame as follows:
 * <ol>
 * <li>Select the next in-flight frame to render</li>
 * <li>Acquire the next frame buffer to be rendered from the swapchain</li>
 * <li>Compose the render sequence for the selected frame</li>
 * <li>Submit the render task</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * TODO
 * If the acquire or present steps fail due to a {@link SwapchainInvalidated} the swapchain and frame buffers are recreated by {@link SwapchainAdapter#recreate()}.
 * <p>
 * The number of in-flight frames can be overridden by the {@link #count(Swapchain)} method.
 * Usually this is the same as the number of swapchain attachments (the default behaviour).
 * <p>
 * @author Sarge
 */
public class RenderTask implements Runnable, TransientObject {
	private final Swapchain swapchain;
	private final Framebuffer.Group group;
	private final FrameComposer composer;
	private final FrameState[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param swapchain			Swapchain
	 * @param group				Frame buffers
	 * @param composer			Composer for the render sequence
	 */
	public RenderTask(Swapchain swapchain, Framebuffer.Group group, FrameComposer composer) {
		this.swapchain = requireNonNull(swapchain);
		this.group = requireNonNull(group);
		this.composer = requireNonNull(composer);
		this.frames = init(swapchain);
	}
	// TODO - swapchain provider

	/**
	 * Builds the array of in-flight frames.
	 * @param swapchain Swapchain
	 * @return In-flight frames
	 */
	private FrameState[] init(Swapchain swapchain) {
		final int count = count(swapchain);
		final var frames = new FrameState[count];
		final var device = swapchain.device();
		for(int n = 0; n < count; ++n) {
			frames[n] = frame(device);
		}
		return frames;
	}

	/**
	 * Determines the number of in-flight frames.
	 * @param swapchain Swapchain
	 * @return Number of in-flight frames
	 */
	protected int count(Swapchain swapchain) {
		return swapchain.attachments().size();
	}

	/**
	 * Instantiates a frame state tracker.
	 * @param device Logical device
	 * @return New frame state
	 */
	protected FrameState frame(LogicalDevice device) {
		return FrameState.create(device);
	}

	@Override
	public void run() {
		try {
			frame();
		}
		catch(SwapchainInvalidated e) {
			// TODO - recreate the swapchain => factory, uses builder?, needs surface capabilities
			group.recreate();
		}
	}

	private void frame() {
		// Select the next in-flight frame
		final FrameState frame = frames[next];
		if(++next >= frames.length) {
			next = 0;
		}

		// Acquire next frame buffer
		final int index = frame.acquire(swapchain);
		final Framebuffer framebuffer = group.get(index);

		// Render frame
		final Buffer sequence = composer.compose(index, framebuffer);
		frame.render(sequence);

		// Present frame
		frame.present(sequence, index, swapchain);
	}

	@Override
	public void destroy() {
		for(FrameState f : frames) {
			f.destroy();
		}
	}
}
