package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.present.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.Invalidated;

/**
 * The <i>render task</i> orchestrates the various components that collaborate to render and present a frame to the swapchain.
 * <p>
 * The rendering process is as follows:
 * <ol>
 * <li>Select the next in-flight frame</li>
 * <li>Acquire the framebuffer to be rendered from the swapchain</li>
 * <li>Compose the render sequence for the frame</li>
 * <li>Submit the render task</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * This implementation aims to fully utilise the multi-threaded nature of the hardware.
 * Multiple <i>in flight</i> frames are rendered and presented concurrently, synchronised by a {@link FrameState}.
 * <p>
 * The swapchain and frame buffers are recreated on demand if the swapchain is {@link Invalidated}.
 * <p>
 * @author Sarge
 */
public class RenderTask implements Runnable {
	private final SwapchainManager manager;
	private final Framebuffer.Factory framebuffers;
	private final FrameComposer composer;
	private final FrameIterator iterator;

	/**
	 * Constructor.
	 * @param manager			Swapchain manager
	 * @param framebuffers		Framebuffers
	 * @param composer			Composer for the render sequence
	 * @param iterator			In-flight frame iterator
	 */
	public RenderTask(SwapchainManager manager, Framebuffer.Factory framebuffers, FrameComposer composer, FrameIterator iterator) {
		this.manager = requireNonNull(manager);
		this.framebuffers = requireNonNull(framebuffers);
		this.composer = requireNonNull(composer);
		this.iterator = requireNonNull(iterator);
		recreate();
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
		final Framebuffer framebuffer = framebuffers.get(index);

		// Render frame
		final Buffer sequence = composer.compose(frame.index(), framebuffer);
		frame.render(sequence);

		// Present frame
		frame.present(sequence, index, swapchain);
	}

	/**
	 * Recreates the swapchain, attachment views and framebuffers.
	 */
	public synchronized void recreate() {
		// Recreate swapchain
		final Swapchain swapchain = manager.recreate();

		// Recreate attachment image-views
		final Dimensions extents = swapchain.extents();
		final LogicalDevice device = swapchain.device();
		for(Attachment attachment : framebuffers.pass().attachments()) {
			attachment.recreate(device, extents);
		}

		// Recreate framebuffers
		framebuffers.recreate(swapchain);
	}
}
