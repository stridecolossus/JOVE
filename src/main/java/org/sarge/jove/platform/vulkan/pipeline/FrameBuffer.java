package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Set;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.View;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class FrameBuffer extends AbstractVulkanObject {
	/**
	 * Creates a frame buffer for the given view.
	 * @param view Swapchain image view
	 * @param pass Render pass
	 * @return New frame buffer
	 */
	public static FrameBuffer create(View view, RenderPass pass) {
		// Build descriptor
		final Image.Extents extents = view.image().descriptor().extents();
		final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = 1;
		info.pAttachments = toPointerArray(Set.of(view));
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO

		// Allocate frame buffer
		final LogicalDevice dev = view.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference buffer = lib.factory().pointer();
		check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

		// Create frame buffer
		return new FrameBuffer(buffer.getValue(), view);
	}

	/**
	 * Helper - Creates the frame buffers for the given swapchain.
	 * @param swapchain		Swapchain
	 * @param pass			Render pass
	 * @return New framebuffers
	 */
	public static Stream<FrameBuffer> create(SwapChain swapchain, RenderPass pass) {
		return swapchain.views().stream().map(view -> create(view, pass));
	}

	private final View view;

	/**
	 * Constructor.
	 * @param handle 	Handle
	 * @param view		Swapchain image-view
	 */
	private FrameBuffer(Pointer handle, View view) {
		super(handle, view.device(), view.device().library()::vkDestroyFramebuffer);
		this.view = notNull(view);
	}

	/**
	 * @return Swapchain image-view for this frame buffer
	 */
	public View view() {
		return view;
	}
}
