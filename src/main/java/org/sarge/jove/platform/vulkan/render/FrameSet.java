package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.image.View;

/**
 * A <i>frame set</i> is a convenience aggregation of a group of frame buffers for a given swapchain.
 * @author Sarge
 */
public class FrameSet implements TransientObject {
	private final Swapchain swapchain;
	private final List<FrameBuffer> buffers;

	/**
	 * Constructor.
	 * @param swapchain			Swapchain
	 * @param pass				Render pass
	 * @param additional		Additional attachments
	 */
	public FrameSet(Swapchain swapchain, RenderPass pass, List<View> additional) {
		// Init buffers
		final List<View> images = swapchain.attachments();
		this.buffers = new ArrayList<>(images.size());
		this.swapchain = notNull(swapchain);

		// Create buffers
		final Dimensions extents = swapchain.extents();
		for(View image : images) {
			// Enumerate attachments
			final var attachments = new ArrayList<View>();
			attachments.add(image);
			attachments.addAll(additional);

			// Create buffer
			final FrameBuffer buffer = FrameBuffer.create(pass, extents, attachments);
			buffers.add(buffer);
		}
	}

	/**
	 * @return Swapchain
	 */
	public Swapchain swapchain() {
		return swapchain;
	}

	/**
	 * Retrieves a frame buffer by swapchain index.
	 * @param index Index
	 * @return Frame buffer
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public FrameBuffer buffer(int index) {
		return buffers.get(index);
	}

	@Override
	public void destroy() {
		for(FrameBuffer b : buffers) {
			b.destroy();
		}
	}
}
