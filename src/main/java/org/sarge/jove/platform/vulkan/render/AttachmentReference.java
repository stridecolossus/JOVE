package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import org.sarge.jove.platform.vulkan.*;

/**
 * An <i>attachment reference</i> specifies an attachment used in a subpass.
 * @see VkAttachmentReference
 * @author Sarge
 */
public record AttachmentReference(Attachment attachment, VkImageLayout layout) {
	/**
	 * Constructor.
	 * @param attachment		Attachment
	 * @param layout			Image layout used during this subpass
	 */
	public AttachmentReference {
		requireNonNull(attachment);
		requireNonNull(layout);
	}
	// TODO - validate layout valid for attachment?

	/**
	 * Builds the descriptor for this reference.
	 * @param index Attachment index
	 * @return Attachment reference descriptor
	 */
	VkAttachmentReference descriptor(int index) {
		final var descriptor = new VkAttachmentReference();
		descriptor.layout = layout;
		descriptor.attachment = requireZeroOrMore(index);
		return descriptor;
	}

	/**
	 * Helper.
	 * Creates a reference for the given attachment with a default image layout.
	 * @param attachment Attachment
	 * @return Attachment reference
	 */
	public static AttachmentReference of(Attachment attachment) {
		final VkImageLayout layout = switch(attachment.type()) {
			case COLOUR -> VkImageLayout.COLOR_ATTACHMENT_OPTIMAL;
			case DEPTH	-> VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
		};
		return new AttachmentReference(attachment, layout);
	}
}
