package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkAttachmentLoadOp.LOAD;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * An <i>attachment description</i> specifies the properties of an attachment.
 * @see VkAttachmentDescription
 * @author Sarge
 */
public record AttachmentDescription(VkFormat format, VkSampleCountFlags samples, LoadStore operation, LoadStore stencil, VkImageLayout initialLayout,VkImageLayout finalLayout) {
	/**
	 * Convenience wrapper for a load-store pair.
	 */
	public record LoadStore(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
		/**
		 * Unused load-store operation.
		 */
		public static final LoadStore DONT_CARE = new LoadStore(VkAttachmentLoadOp.DONT_CARE, VkAttachmentStoreOp.DONT_CARE);

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
	 * Constructor.
	 * @param format			Image format
	 * @param samples			Number of samples
	 * @param operation			Attachment operations
	 * @param stencil			Stencil operations
	 * @param initialLayout		Initial layout
	 * @param finalLayout		Final layout
	 * @throws IllegalArgumentException if {@link #format} is undefined
	 * @throws IllegalArgumentException if {@link #finalLayout} is invalid for this attachment
	 * @throws IllegalArgumentException if {@link #initialLayout} is undefined and any load operation is {@link VkAttachmentLoadOp#LOAD}
	 */
	public AttachmentDescription {
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
	 * Creates description for a colour attachment for presentation.
	 * @param format Colour image layout
	 * @return Colour attachment
	 */
	public static AttachmentDescription colour(VkFormat format) {
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
	public static AttachmentDescription depth(VkFormat format) {
		return new Builder()
				.format(format)
				.operation(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.DONT_CARE))
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();
	}

	/**
	 * Builder for an attachment description.
	 */
	public static class Builder {
		private final ReverseMapping<VkSampleCountFlags> mapping = ReverseMapping.mapping(VkSampleCountFlags.class);

		private VkFormat format;
		private VkSampleCountFlags samples = VkSampleCountFlags.COUNT_1;
		private LoadStore operation = LoadStore.DONT_CARE;
		private LoadStore stencil = LoadStore.DONT_CARE;
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
		public AttachmentDescription build() {
			return new AttachmentDescription(format, samples, operation, stencil, initialLayout, finalLayout);
		}
	}
}
