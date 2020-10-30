package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.util.List;

import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass} and is comprised of a number of image attachments.
 * @author Sarge
 */
public class FrameBuffer extends AbstractVulkanObject {
	/**
	 * Creates a frame buffer for the given view.
	 * @param view Swapchain image view
	 * @param pass Render pass
	 * @return New frame buffer
	 */
	public static FrameBuffer create(List<View> views, RenderPass pass) {
		// Use extents of first attachment
		Check.notEmpty(views);
		final Image.Extents extents = views.get(0).image().descriptor().extents();

		// Build descriptor
		final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = views.size();
		info.pAttachments = Handle.toPointerArray(views);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO

		// Allocate frame buffer
		final LogicalDevice dev = pass.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference buffer = lib.factory().pointer();
		check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

		// Create frame buffer
		return new FrameBuffer(buffer.getValue(), dev, extents, views);
	}

	private final List<View> attachments;
	private final Image.Extents extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param extents			Image extents
	 * @param attachments		Image attachments
	 */
	private FrameBuffer(Pointer handle, LogicalDevice dev, Image.Extents extents, List<View> attachments) {
		super(handle, dev, dev.library()::vkDestroyFramebuffer);
		this.extents = notNull(extents);
		this.attachments = List.copyOf(notEmpty(attachments));
	}

	/**
	 * @return Extents of the frame-buffer attachments
	 */
	public Image.Extents extents() {
		return extents;
	}

	/**
	 * @return Image attachments
	 */
	public List<View> attachments() {
		return attachments;
	}
}
