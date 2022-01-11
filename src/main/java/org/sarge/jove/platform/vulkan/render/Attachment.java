package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkAttachmentDescription;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.render.Attachment.Operations;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * An <i>attachment</i> defines a target for a render pass such as a colour or depth-stencil image.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record Attachment(VkFormat format, VkSampleCount samples, Operations colour, Operations stencil, VkImageLayout initial, VkImageLayout layout) {
	/**
	 * Attachment operations.
	 */
	public static record Operations(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
		/**
		 * Constructor.
		 * @param load			Load operation
		 * @param store			Store operation
		 */
		public Operations {
			Check.notNull(load);
			Check.notNull(store);
		}
	}

	/**
	 * Constructor.
	 * @param format		Attachment format
	 * @param samples		Number of samples
	 * @param colour		Colour attachment operations
	 * @param stencil		Depth-stencil operations
	 * @param initial		Initial layout
	 * @param layout		New layout
	 */
	public Attachment {
		Check.notNull(format);
		Check.notNull(samples);
		Check.notNull(colour);
		Check.notNull(stencil);
		Check.notNull(initial);
		Check.notNull(layout);
	}

	/**
	 * Populates a descriptor for this descriptor.
	 * @param desc Attachment descriptor
	 */
	void populate(VkAttachmentDescription attachment) {
		attachment.format = format;
		attachment.samples = samples;
		attachment.loadOp = colour.load;
		attachment.storeOp = colour.store;
		attachment.stencilLoadOp = stencil.load;
		attachment.stencilStoreOp = stencil.store;
		attachment.initialLayout = initial;
		attachment.finalLayout = layout;
	}

	/**
	 * Builder for a render pass attachment.
	 */
	public static class Builder {
		private VkFormat format;
		private VkSampleCount samples = VkSampleCount.COUNT_1;
		private VkAttachmentLoadOp load = VkAttachmentLoadOp.DONT_CARE;
		private VkAttachmentStoreOp store = VkAttachmentStoreOp.DONT_CARE;
		private VkAttachmentLoadOp stencilLoad = VkAttachmentLoadOp.DONT_CARE;
		private VkAttachmentStoreOp stencilStore = VkAttachmentStoreOp.DONT_CARE;
		private VkImageLayout initial = VkImageLayout.UNDEFINED;
		private VkImageLayout layout;

		/**
		 * Sets the attachment format (usually the same as the swap-chain).
		 * @param format Attachment format
		 */
		public Builder format(VkFormat format) {
			// TODO - check undefined? or is that valid?
			this.format = notNull(format);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Number of samples
		 */
		public Builder samples(VkSampleCount samples) {
			this.samples = notNull(samples);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Sample count
		 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
		 * @see #samples(VkSampleCount)
		 */
		public Builder samples(int samples) {
			this.samples = IntegerEnumeration.mapping(VkSampleCount.class).map(samples);
			return this;
		}

		/**
		 * Sets the attachment load operation (before rendering).
		 * @param op Load operation
		 */
		public Builder load(VkAttachmentLoadOp op) {
			this.load = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment store operation (after rendering).
		 * @param op Store operation
		 */
		public Builder store(VkAttachmentStoreOp op) {
			this.store = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil load operation (before rendering).
		 * @param op Stencil load operation
		 */
		public Builder stencilLoad(VkAttachmentLoadOp op) {
			this.stencilLoad = notNull(op);
			return this;
		}

		/**
		 * Sets the attachment stencil store operation (after rendering).
		 * @param op Stencil store operation
		 */
		public Builder stencilStore(VkAttachmentStoreOp op) {
			this.stencilStore = notNull(op);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			this.initial = notNull(layout);
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
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @throws IllegalArgumentException if the attachment format or final layout have not been specified
		 * @return New attachment
		 */
		public Attachment build() {
			// Validate
			if(format == null) throw new IllegalArgumentException("No format specified for attachment");
			if(layout == null) throw new IllegalArgumentException("No final layout specified");

			// Create attachment
			final Operations colour = new Operations(load, store);
			final Operations stencil = new Operations(stencilLoad, stencilStore);
			return new Attachment(format, samples, colour, stencil, initial, layout);
		}
	}
}
