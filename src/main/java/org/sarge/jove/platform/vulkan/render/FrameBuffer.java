package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.image.Descriptor;
import org.sarge.jove.platform.vulkan.image.View;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass} and is comprised of a number of image attachments.
 * @author Sarge
 */
public class FrameBuffer extends AbstractVulkanObject {
	/**
	 * Creates a frame buffer for the given view.
	 * @param attachments		Image attachments
	 * @param pass				Render pass
	 * @return New frame buffer
	 * @throws IllegalArgumentException if the number of attachments does not match the render pass
	 */
	public static FrameBuffer create(List<View> attachments, RenderPass pass) {
		// Check correct number of attachments
		if(pass.count() != attachments.size()) {
			throw new IllegalArgumentException(String.format("Number of attachments does not match the render pass: attachments=%d pass=%s", attachments.size(), pass));
		}
		// TODO - for each view/attachment check matches pass[n], attachment type (aspects), and format?

		// Use extents of first attachment
		final Iterator<View> itr = attachments.iterator();
		final Descriptor.Extents extents = itr.next().extents();

		// Check all attachments have the same extents
		while(itr.hasNext()) {
			final View attachment = itr.next();
			if(!extents.equals(attachment.extents())) {
				throw new IllegalArgumentException(String.format("Attachments extents mismatch: expected=%s attachment=%s", extents, attachment));
			}
		}

		// Build descriptor
		final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = attachments.size();
		info.pAttachments = Handle.toArray(attachments);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO

		// Allocate frame buffer
		final DeviceContext dev = pass.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference buffer = lib.factory().pointer();
		check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

		// Create frame buffer
		return new FrameBuffer(buffer.getValue(), dev, extents, attachments);
	}

	private final List<View> attachments;
	private final Descriptor.Extents extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param extents			Image extents
	 * @param attachments		Image attachments
	 */
	private FrameBuffer(Pointer handle, DeviceContext dev, Descriptor.Extents extents, List<View> attachments) {
		super(handle, dev);
		this.extents = notNull(extents);
		this.attachments = List.copyOf(notEmpty(attachments));
	}

	/**
	 * @return Extents of the frame-buffer attachments
	 */
	public Descriptor.Extents extents() {
		return extents;
	}

	/**
	 * @return Image attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	@Override
	protected Destructor destructor(VulkanLibrary lib) {
		return lib::vkDestroyFramebuffer;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("extents", extents)
				.append("attachments", attachments)
				.build();
	}
}
