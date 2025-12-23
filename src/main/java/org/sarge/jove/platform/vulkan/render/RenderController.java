package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.present.*;

/**
 * The <i>render controller</i> composes the components used during rendering and manages recreation on swapchain invalidation.
 * @author Sarge
 */
public class RenderController {
	private final SwapchainManager manager;
	private final Framebuffer.Factory framebuffers;
	private final LogicalDevice device;

	/**
	 * Constructor.
	 * @param manager			Recreates the swapchain
	 * @param framebuffers		Framebuffers
	 * @param allocator			Memory allocator for attachment image-views
	 */
	public RenderController(SwapchainManager manager, Framebuffer.Factory framebuffers, LogicalDevice device) {
		this.manager = requireNonNull(manager);
		this.framebuffers = requireNonNull(framebuffers);
		this.device = requireNonNull(device);
		build(manager.swapchain());
	}

	public Swapchain swapchain() {
		return manager.swapchain();
	}

	public Framebuffer.Factory framebuffers() {
		return framebuffers;
	}

	/**
	 * Recreates the swapchain, attachments and framebuffers.
	 */
	public void recreate() {
		// Wait for current rendering work to complete
		device.waitIdle();

		// Recreate swapchain, attachments and framebuffers
		final var swapchain = manager.recreate();
		build(swapchain);
	}

	/**
	 * Builds the attachment image-views and framebuffers.
	 */
	private void build(Swapchain swapchain) {
		build(framebuffers.pass(), swapchain.extents());
		framebuffers.recreate(swapchain);
	}

	/**
	 * Builds the image-views for the attachments.
	 */
	private void build(RenderPass pass, Dimensions extents) {
		for(Attachment attachment : pass.attachments()) {
			attachment.recreate(device, extents);
		}
	}
}
