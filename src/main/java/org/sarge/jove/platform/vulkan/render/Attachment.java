package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkAttachmentDescription;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkSampleCount;

/**
 * An <i>attachment</i> defines a target for a render pass such as a colour or depth-stencil image.
 * @author Sarge
 */
public class Attachment {
	private final VkAttachmentDescription desc;

	/**
	 * Constructor.
	 * @param desc Attachment description
	 */
	private Attachment(VkAttachmentDescription desc) {
		this.desc = notNull(desc);
	}

	/**
	 * @return Attachment format
	 */
	public VkFormat format() {
		return desc.format;
	}

	/**
	 * Populates a descriptor for this descriptor.
	 * @param desc Attachment descriptor
	 */
	void populate(VkAttachmentDescription attachment) {
		attachment.format = desc.format;
		attachment.samples = desc.samples;
		attachment.loadOp = desc.loadOp;
		attachment.storeOp = desc.storeOp;
		attachment.stencilLoadOp = desc.stencilLoadOp;
		attachment.stencilStoreOp = desc.stencilStoreOp;
		attachment.initialLayout = desc.initialLayout;
		attachment.finalLayout = desc.finalLayout;
	}
	// TODO - factor out fields rather than using descriptor? builder can manipulate directly => make this class final?

	@Override
	public int hashCode() {
		return desc.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this);
	}

	@Override
	public String toString() {
		return desc.toString();
	}

	/**
	 * Builder for a render pass attachment.
	 */
	public static class Builder {
		private VkAttachmentDescription desc = new VkAttachmentDescription();

		public Builder() {
			samples(VkSampleCount.COUNT_1);
			load(VkAttachmentLoadOp.DONT_CARE);
			store(VkAttachmentStoreOp.DONT_CARE);
			stencilLoad(VkAttachmentLoadOp.DONT_CARE);
			stencilStore(VkAttachmentStoreOp.DONT_CARE);
			initialLayout(VkImageLayout.UNDEFINED);
		}

		/**
		 * Sets the attachment format (usually the same as the swap-chain).
		 * @param format Attachment format
		 */
		public Builder format(VkFormat format) {
			// TODO - check undefined? or is that valid?
			desc.format = notNull(format);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Number of samples
		 */
		public Builder samples(VkSampleCount samples) {
			desc.samples = notNull(samples);
			return this;
		}

		/**
		 * Sets the attachment load operation (before rendering).
		 * @param op Load operation
		 */
		public Builder load(VkAttachmentLoadOp op) {
			desc.loadOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment store operation (after rendering).
		 * @param op Store operation
		 */
		public Builder store(VkAttachmentStoreOp op) {
			desc.storeOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil load operation (before rendering).
		 * @param op Stencil load operation
		 */
		public Builder stencilLoad(VkAttachmentLoadOp op) {
			desc.stencilLoadOp = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil store operation (after rendering).
		 * @param op Stencil store operation
		 */
		public Builder stencilStore(VkAttachmentStoreOp op) {
			desc.stencilStoreOp = notNull(op);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			desc.initialLayout = notNull(layout);
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
			desc.finalLayout = notNull(layout);
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @throws IllegalArgumentException if the attachment format or final layout have not been specified
		 * @return New attachment
		 */
		public Attachment build() {
			// Validate
			if(desc.format == null) throw new IllegalArgumentException("No format specified for attachment");
			if(desc.finalLayout == null) throw new IllegalArgumentException("No final layout specified");

			// Create attachment
			try {
				return new Attachment(desc);
			}
			finally {
				desc = null;
			}
		}
	}
}
