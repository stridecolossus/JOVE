package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.util.IntEnum;
import org.sarge.lib.util.Check;

/**
 * An <i>attachment</i> defines a target for a render pass such as colour or depth-stencil images.
 * @see VkAttachmentDescription
 * @author Sarge
 */
public record Attachment(VkFormat format, VkSampleCount samples, Attachment.Operations colour, Attachment.Operations depthStencil, VkImageLayout before, VkImageLayout after) {
	/**
	 * Convenience wrapper for load-store operations.
	 */
	public record Operations(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
		/**
		 * Default load-store operations.
		 */
		public static final Operations DONT_CARE = new Operations(VkAttachmentLoadOp.DONT_CARE, VkAttachmentStoreOp.DONT_CARE);

		/**
		 * Convenience load-store operations for the colour attachment.
		 */
		public static final Operations COLOUR = new Operations(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE);

		/**
		 * Convenience load-store operations for the depth-stencil attachment.
		 */
		public static final Operations DEPTH_STENCIL = new Operations(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.DONT_CARE);

		/**
		 * Constructor.
		 * @param load		Load operation
		 * @param store		Store operation
		 */
		public Operations {
			Check.notNull(load);
			Check.notNull(store);
		}
	}

	/**
	 * Constructor.
	 * @param format			Attachment format
	 * @param samples			Number of samples
	 * @param colour			Colour load-store operations
	 * @param depthStencil		Depth-stencil operations
	 * @param before			Initial layout
	 * @param after				Final layout
	 * @throws IllegalArgumentException if the {@link #after} layout is unspecified or is invalid
	 */
	public Attachment {
		if(format == null) throw new IllegalArgumentException("No format specified for attachment");
		if(after == null) throw new IllegalArgumentException("No final layout specified");
		switch(after) {
			case UNDEFINED, PREINITIALIZED -> throw new IllegalArgumentException("Invalid final layout: " + after);
		}
		Check.notNull(samples);
		Check.notNull(colour);
		Check.notNull(depthStencil);
		Check.notNull(before);
	}

	/**
	 * Creates an attachment with default configuration.
	 * @param format		Attachment format
	 * @param after			Final layout
	 * @return New attachment
	 * @see #Attachment(VkFormat, VkSampleCount, Operations, Operations, VkImageLayout, VkImageLayout)
	 */
	public static Attachment of(VkFormat format, VkImageLayout after) {
		return new Builder()
				.format(format)
				.finalLayout(after)
				.build();
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
		attachment.stencilLoadOp = depthStencil.load;
		attachment.stencilStoreOp = depthStencil.store;
		attachment.initialLayout = before;
		attachment.finalLayout = after;
	}

	/**
	 * Builder for an attachment.
	 */
	public static class Builder {
		private VkFormat format;
		private VkSampleCount samples = VkSampleCount.COUNT_1;
		private Operations colour = Operations.DONT_CARE;
		private Operations depthStencil = Operations.DONT_CARE;
		private VkImageLayout before = VkImageLayout.UNDEFINED;
		private VkImageLayout after;

		/**
		 * Sets the attachment format.
		 * @param format Attachment format
		 */
		public Builder format(VkFormat format) {
			// TODO - check undefined? or is that valid?
			this.format = notNull(format);
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
			this.samples = IntEnum.reverse(VkSampleCount.class).map(samples);
			return this;
		}

		/**
		 * Sets the load/store operations for the colour attachment.
		 * @param colour Colour operations
		 */
		public Builder colour(Operations colour) {
			this.colour = notNull(colour);
			return this;
		}

		/**
		 * Sets the load/store operations for the depth-stencil attachment.
		 * @param depthStencil Depth-stencil operations
		 */
		public Builder depth(Operations depthStencil) {
			this.depthStencil = notNull(depthStencil);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			this.before = notNull(layout);
			return this;
		}

		/**
		 * Sets the final image layout.
		 * @param layout final image layout
		 */
		public Builder finalLayout(VkImageLayout layout) {
			this.after = notNull(layout);
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @throws IllegalArgumentException if the attachment format or final layout have not been specified
		 * @return New attachment
		 */
		public Attachment build() {
			if(format == null) throw new IllegalArgumentException("No format specified for attachment");
			if(after == null) throw new IllegalArgumentException("No final layout specified");
			return new Attachment(format, samples, colour, depthStencil, before, after);
		}
	}
}
