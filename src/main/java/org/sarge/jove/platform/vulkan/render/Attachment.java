package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkAttachmentLoadOp.LOAD;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * An <i>attachment</i> defines a target for a render pass such as a colour or depth-stencil image.
 * @see VkAttachmentDescription
 * @author Sarge
 */
public record Attachment(VkFormat format, VkSampleCountFlags samples, Attachment.LoadStore operation, Attachment.LoadStore stencil, VkImageLayout initialLayout, VkImageLayout finalLayout) {
	/**
	 * Convenience wrapper for a load-store pair.
	 */
	public record LoadStore(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
		/**
		 * Constructor.
		 * @param load		Load operation
		 * @param store		Store operation
		 */
		public LoadStore {
			requireNonNull(load);
			requireNonNull(store);
		}
	}

	/**
	 * Creates a colour attachment for presentation.
	 * @param format Colour image layout
	 * @return Colour attachment
	 */
	public static Attachment colour(VkFormat format) {
		return new Builder()
				.format(format)
				.operation(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE))
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();
	}

	/**
	 * Creates the depth-stencil attachment.
	 * @param format Depth format
	 * @return Depth attachment
	 */
	public static Attachment depth(VkFormat format) {
		return new Builder()
				.format(format)
				.operation(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.DONT_CARE))
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();
	}

	/**
	 * Constructor.
	 * @param format			Image format
	 * @param samples			Number of samples
	 * @param operation			Attachment operations
	 * @param stencil			Stencil operations
	 * @param initialLayout		Initial layout
	 * @param finalLayout		Final layout
	 * @throws IllegalArgumentException if {@link #format} is undefined
	 * @throws IllegalArgumentException if {@link #finalLayout} is invalid for this attachment
	 * @throws IllegalArgumentException if {@link #initialLayout} is undefined and the load operation is {@link VkAttachmentLoadOp#LOAD}
	 */
	public Attachment {
		requireNonNull(format);
		requireNonNull(samples);
		requireNonNull(operation);
		requireNonNull(stencil);
		requireNonNull(initialLayout);
		requireNonNull(finalLayout);

		if(format == VkFormat.UNDEFINED) {
			throw new IllegalArgumentException("Format cannot be undefined");
		}

		switch(finalLayout) {
    		case UNDEFINED, PREINITIALIZED -> {
    			throw new IllegalArgumentException("Invalid final layout: " + finalLayout);
    		}
    	}

		if((initialLayout == VkImageLayout.UNDEFINED) && ((operation.load == LOAD) || (stencil.load == LOAD))) {
			throw new IllegalArgumentException("Cannot load an undefined layout");
		}
	}

	/**
	 * @return Attachment descriptor
	 */
	VkAttachmentDescription populate() {
		final var attachment = new VkAttachmentDescription();
		attachment.format = format;
		attachment.samples = new EnumMask<>(samples);
		attachment.loadOp = operation.load;
		attachment.storeOp = operation.store;
		attachment.stencilLoadOp = stencil.load;
		attachment.stencilStoreOp = stencil.store;
		attachment.initialLayout = initialLayout;
		attachment.finalLayout = finalLayout;
		return attachment;
	}

	/**
	 * Builder for an attachment.
	 */
	public static class Builder {
		private final LoadStore none = new LoadStore(VkAttachmentLoadOp.DONT_CARE, VkAttachmentStoreOp.DONT_CARE);
		private final ReverseMapping<VkSampleCountFlags> mapping = ReverseMapping.mapping(VkSampleCountFlags.class);

		private VkFormat format;
		private VkSampleCountFlags samples = VkSampleCountFlags.COUNT_1;
		private LoadStore operation = none;
		private LoadStore stencil = none;
		private VkImageLayout initialLayout = VkImageLayout.UNDEFINED;
		private VkImageLayout finalLayout;

		/**
		 * Sets the image format of this attachment.
		 * @param format Image format
		 */
		public Builder format(VkFormat format) {
			this.format = format;
			return this;
		}

		/**
		 * Sets the number of samples (default is one).
		 * @param samples Number of samples
		 */
		public Builder samples(VkSampleCountFlags samples) {
			this.samples = samples;
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Sample count
		 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
		 * @see #samples(VkSampleCount)
		 */
		public Builder samples(int samples) {
			this.samples = mapping.map(samples);
			return this;
		}

		/**
		 * Sets the load-store operations for a colour or depth attachment.
		 * @param operation Attachment operations
		 */
		public Builder operation(LoadStore operation) {
			this.operation = operation;
			return this;
		}

		/**
		 * Sets the load-store operations for the stencil.
		 * @param stencil Stencil operation
		 */
		public Builder stencil(LoadStore stencil) {
			this.stencil = stencil;
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param layout Initial image layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			this.initialLayout = layout;
			return this;
		}

		/**
		 * Sets the final layout.
		 * @param layout Final image layout
		 */
		public Builder finalLayout(VkImageLayout layout) {
			this.finalLayout = layout;
			return this;
		}

		/**
		 * Constructs this attachment.
		 * @return New attachment
		 */
		public Attachment build() {
			return new Attachment(format, samples, operation, stencil, initialLayout, finalLayout);
		}
	}
}
