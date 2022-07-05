package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * An <i>attachment</i> defines a target for a render pass such as a colour or depth-stencil image.
 * @see VkAttachmentDescription
 * @author Sarge
 */
public class Attachment {
	private final VkAttachmentDescription descriptor;

	private Attachment(VkAttachmentDescription attachment) {
		this.descriptor = notNull(attachment);
	}

	/**
	 * @return Attachment image format
	 */
	public VkFormat format() {
		return descriptor.format;
	}

	/**
	 * Populates a descriptor for this descriptor.
	 * @param desc Attachment descriptor
	 */
	void populate(VkAttachmentDescription attachment) {
		attachment.format = descriptor.format;
		attachment.samples = descriptor.samples;
		attachment.loadOp = descriptor.loadOp;
		attachment.storeOp = descriptor.storeOp;
		attachment.stencilLoadOp = descriptor.stencilLoadOp;
		attachment.stencilStoreOp = descriptor.stencilStoreOp;
		attachment.initialLayout = descriptor.initialLayout;
		attachment.finalLayout = descriptor.finalLayout;
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Attachment that) &&
				(descriptor.format == that.descriptor.format) &&
				(descriptor.samples == that.descriptor.samples) &&
				(descriptor.loadOp == that.descriptor.loadOp) &&
				(descriptor.storeOp == that.descriptor.storeOp) &&
				(descriptor.stencilLoadOp == that.descriptor.stencilLoadOp) &&
				(descriptor.stencilStoreOp == that.descriptor.stencilStoreOp) &&
				(descriptor.initialLayout == that.descriptor.initialLayout) &&
				(descriptor.finalLayout == that.descriptor.finalLayout);
	}

	@Override
	public String toString() {
		return descriptor.toString();
	}

	/**
	 * Builder for a render pass attachment.
	 */
	public static class Builder {
		private final VkAttachmentDescription attachment = new VkAttachmentDescription();

		public Builder() {
			attachment.samples = VkSampleCount.COUNT_1;
			attachment.loadOp = VkAttachmentLoadOp.DONT_CARE;
			attachment.storeOp = VkAttachmentStoreOp.DONT_CARE;
			attachment.stencilLoadOp = VkAttachmentLoadOp.DONT_CARE;
			attachment.stencilStoreOp = VkAttachmentStoreOp.DONT_CARE;
			attachment.initialLayout = VkImageLayout.UNDEFINED;
		}

		/**
		 * Sets the attachment format.
		 * @param format Attachment format
		 */
		public Builder format(VkFormat format) {
			// TODO - check undefined? or is that valid?
			attachment.format = notNull(format);
			return this;
		}

		/**
		 * Helper - Sets the attachment format to that of the given image view.
		 * @param view Image view
		 */
		public Builder format(View view) {
			final VkFormat format = view.image().descriptor().format();
			return format(format);
		}

		/**
		 * Sets the number of samples.
		 * @param samples Number of samples
		 */
		public Builder samples(VkSampleCount samples) {
			attachment.samples = notNull(samples);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Sample count
		 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
		 * @see #samples(VkSampleCount)
		 */
		public Builder samples(int samples) {
			attachment.samples = IntegerEnumeration.mapping(VkSampleCount.class).map(samples);
			return this;
		}

		/**
		 * Sets the attachment load operation (before rendering).
		 * @param op Load operation
		 */
		public Builder load(VkAttachmentLoadOp op) {
			attachment.loadOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment store operation (after rendering).
		 * @param op Store operation
		 */
		public Builder store(VkAttachmentStoreOp op) {
			attachment.storeOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil load operation (before rendering).
		 * @param op Stencil load operation
		 */
		public Builder stencilLoad(VkAttachmentLoadOp op) {
			attachment.stencilLoadOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil store operation (after rendering).
		 * @param op Stencil store operation
		 */
		public Builder stencilStore(VkAttachmentStoreOp op) {
			attachment.stencilStoreOp = notNull(op);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			attachment.initialLayout = notNull(layout);
			return this;
		}

		/**
		 * Sets the final image layout.
		 * @param layout final image layout
		 */
		public Builder finalLayout(VkImageLayout layout) {
			if((layout == VkImageLayout.UNDEFINED) || (layout == VkImageLayout.PREINITIALIZED)) {
				throw new IllegalArgumentException("Invalid final layout: " + layout);
			}
			attachment.finalLayout = notNull(layout);
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @throws IllegalArgumentException if the attachment format or final layout have not been specified
		 * @return New attachment
		 */
		public Attachment build() {
			if(attachment.format == null) throw new IllegalArgumentException("No format specified for attachment");
			if(attachment.finalLayout == null) throw new IllegalArgumentException("No final layout specified");
			return new Attachment(attachment);
		}
	}
}
