package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * The <i>swapchain adapter</i> manages the swapchain and associated frame buffers.
 * <p>
 * The {@link #recreate()} rebuilds the swapchain and frame buffers if the surface has been invalidated indicated by a {@link SwapchainInvalidated}.
 * <p>
 * @author Sarge
 */
public class SwapchainAdapter implements TransientObject {
	private final RenderPass pass;
	private final Swapchain.Builder builder;
	private final List<View> additional;
	private FrameBuffer[] buffers;
	private Swapchain swapchain;

	/**
	 * Constructor.
	 * @param builder			Swapchain builder
	 * @param pass				Render pass
	 * @param additional		Additional attachments
	 */
	public SwapchainAdapter(Swapchain.Builder builder, RenderPass pass, List<View> additional) {
		this.pass = requireNonNull(pass);
		this.builder = requireNonNull(builder);
		this.additional = List.copyOf(additional);
		createSwapchain();
		createBuffers();
	}

	/**
	 * @return Swapchain
	 */
	public Swapchain swapchain() {
		return swapchain;
	}

	/**
	 * Retrieves a frame buffer by index.
	 * @param index Frame buffer index
	 * @return Frame buffer
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public FrameBuffer buffer(int index) {
		return buffers[index];
	}

	/**
	 * Recreates the swapchain and frame buffers.
	 * @see SwapchainInvalidated
	 */
	public void recreate() {
		// Wait for all rendering work to complete
		// TODO
		// swapchain.device().waitIdle();

		// Release swapchain and buffers
		destroy();

		// Recreate swapchain
		// TODO - builder.update(); // TODO - nasty
		createSwapchain();
		createBuffers();
	}

	/**
	 * Recreates the swapchain.
	 */
	private void createSwapchain() {
		// TODO - will need to update surface capabilities => builder.update();
		swapchain = builder.build(pass.device());
	}

	/**
	 * Recreates the frame buffers.
	 */
	private void createBuffers() {
		final var extents = new Rectangle(swapchain.extents());
		buffers = swapchain
				.attachments()
				.stream()
				.map(this::attachments)
				.map(attachments -> FrameBuffer.create(pass, extents, attachments))
				.toArray(FrameBuffer[]::new);
	}

	/**
	 * @param col Colour attachment
	 * @return The set of attachments for each frame buffer
	 */
	private List<View> attachments(View col) {
		final List<View> attachments = new ArrayList<>();
		attachments.add(col);
		attachments.addAll(additional);
		return attachments;
	}

	@Override
	public void destroy() {
		swapchain.destroy();
		for(FrameBuffer fb : buffers) {
			fb.destroy();
		}
	}
}
