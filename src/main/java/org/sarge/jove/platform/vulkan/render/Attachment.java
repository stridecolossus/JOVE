package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;

/**
 * An <i>attachment</i> is a target of the rendering process, such as a swapchain image or the depth-stencil.
 * <p>
 * This class composes the various attachment properties used during rendering and also explicitly specifies an {@link AttachmentType}.
 * Note there is no direct Vulkan equivalent for this type.
 * <p>
 * @author Sarge
 */
public interface Attachment {
	/**
	 * Types of attachment.
	 */
	enum AttachmentType {
		COLOUR,
		DEPTH,
		RESOLVE
	}

	/**
	 * @return Type of attachment
	 */
	AttachmentType type();

	/**
	 * @return Image format
	 */
	VkFormat format();

	/**
	 * @return Attachment description
	 */
	AttachmentDescription description();

	/**
	 * Retrieves an image view of this attachment by index.
	 * Attachments with a single view (such as the depth-stencil) should ignore the index and return the same view for all frames.
	 * @param index Frame index
	 * @return Attachment view for the given index
	 */
	View view(int index);

	/**
	 * @return Clear value for this attachment
	 */
	ClearValue clear();

	/**
	 * Recreates the image-view(s) of this attachment when the swapchain has become invalid.
	 * @param device		Logical device
	 * @param extents		Swapchain extents
	 */
	void recreate(LogicalDevice device, Dimensions extents);

	/**
	 * An <i>attachment reference</i> configures this attachment for use in a subpass.
	 */
	record Reference(Attachment attachment, VkImageLayout layout) {
		/**
		 * Constructor.
		 * @param attachment		Attachment
		 * @param layout			Image layout used during this subpass
		 */
		public Reference {
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
	}
}
