package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.VkAttachmentLoadOp.LOAD;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntEnum;
import org.sarge.lib.util.Check;

/**
 * An <i>attachment</i> defines a target for a render pass such as colour or depth-stencil images.
 * @see VkAttachmentDescription
 * @author Sarge
 */
public record Attachment(VkFormat format, VkSampleCount samples, Attachment.LoadStore attachment, Attachment.LoadStore stencil, VkImageLayout initialLayout, VkImageLayout finalLayout) {
	/**
	 * Convenience wrapper for a load-store operations pair.
	 */
	public record LoadStore(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
		/**
		 * Constructor.
		 * @param load		Load operation
		 * @param store		Store operation
		 */
		public LoadStore {
			Check.notNull(load);
			Check.notNull(store);
		}
	}

	/**
	 * Creates a simple colour attachment for presentation.
	 * @param format Colour image layout
	 * @return Colour attachment
	 */
	public static Attachment colour(VkFormat format) {
		return new Builder(format)
				.attachment(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE))
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();
	}

	/**
	 * Creates a simple depth attachment.
	 * @param format Depth image layout
	 * @return Depth attachment
	 */
	public static Attachment depth(VkFormat format) {
		return new Builder(format)
				.attachment(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.DONT_CARE))
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();
	}

	/**
	 * Constructor.
	 * @param format			Image format
	 * @param samples			Number of samples
	 * @param attachment		Attachment operations
	 * @param stencil			Stencil operations
	 * @param initialLayout		Initial layout
	 * @param finalLayout		Final layout
	 * @throws IllegalArgumentException if {@link #format} is undefined
	 * @throws IllegalArgumentException if {@link #finalLayout} is invalid for this attachment
	 * @throws IllegalArgumentException if {@link #initialLayout} is undefined and the load operation is {@link VkAttachmentLoadOp#LOAD}
	 */
	public Attachment {
		Check.notNull(format);
		Check.notNull(samples);
		Check.notNull(attachment);
		Check.notNull(stencil);
		Check.notNull(initialLayout);
		Check.notNull(finalLayout);

		if(format == VkFormat.UNDEFINED) throw new IllegalArgumentException("Format cannot be undefined");

		switch(finalLayout) {
    		case UNDEFINED, PREINITIALIZED -> throw new IllegalArgumentException("Invalid final layout: " + finalLayout);
    	}

		if(initialLayout == VkImageLayout.UNDEFINED) {
			if(attachment.load == LOAD) throw new IllegalArgumentException("Cannot load an image with an undefined layout");
			if(stencil.load == LOAD) throw new IllegalArgumentException("Cannot load a stencil with an undefined layout");
		}

		// TODO - further validation
		// - no attachment load-store if stencil only
		// - no stencil load-store if colour image
	}

	/**
	 * Populates the Vulkan descriptor for this attachment.
	 * @param desc Attachment descriptor
	 */
	void populate(VkAttachmentDescription desc) {
		desc.format = format;
		desc.samples = samples;
		desc.loadOp = attachment.load;
		desc.storeOp = attachment.store;
		desc.stencilLoadOp = stencil.load;
		desc.stencilStoreOp = stencil.store;
		desc.initialLayout = initialLayout;
		desc.finalLayout = finalLayout;
	}

	/**
	 * Builder for an attachment.
	 */
	public static class Builder {
		private static final LoadStore DONT_CARE = new LoadStore(VkAttachmentLoadOp.DONT_CARE, VkAttachmentStoreOp.DONT_CARE);

		private final VkFormat format;
		private VkSampleCount samples = VkSampleCount.COUNT_1;
		private LoadStore attachment = DONT_CARE;
		private LoadStore stencil = DONT_CARE;
		private VkImageLayout initialLayout = VkImageLayout.UNDEFINED;
		private VkImageLayout finalLayout;

		/**
		 * Constructor.
		 * @param format Image format
		 */
		public Builder(VkFormat format) {
			this.format = notNull(format);
		}

		/**
		 * Sets the number of samples (default is one).
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
		 * Sets the load-store operations for a colour or depth attachment.
		 * @param load Attachment operations
		 */
		public Builder attachment(LoadStore attachment) {
			this.attachment = notNull(attachment);
			return this;
		}

		/**
		 * Sets the load-store operations for the stencil.
		 * @param stencil Stencil operation
		 */
		public Builder stencil(LoadStore stencil) {
			this.stencil = notNull(stencil);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			this.initialLayout = notNull(layout);
			return this;
		}

		/**
		 * Sets the final layout.
		 * @param layout Final image layout
		 */
		public Builder finalLayout(VkImageLayout layout) {
			this.finalLayout = notNull(layout);
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @return New attachment
		 */
		public Attachment build() {
			return new Attachment(format, samples, attachment, stencil, initialLayout, finalLayout);
		}
	}
}
