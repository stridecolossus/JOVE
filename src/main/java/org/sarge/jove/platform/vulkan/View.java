package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> TODO
 * @author Sarge
 */
public class View {
	private final Pointer handle;
	private final Image image;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param image		Underlying image
	 */
	private View(Pointer handle, Image image) {
		this.handle = notNull(handle);
		this.image = notNull(image);
	}

	/**
	 * @return Image view handle
	 */
	Pointer handle() {
		return handle;
	}

	/**
	 * @return Underlying image
	 */
	public Image image() {
		return image;
	}

	/**
	 * Destroys this view.
	 */
	public void destroy() {
		final LogicalDevice dev = image.device();
		dev.library().vkDestroyImageView(dev.handle(), handle, null);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Builder for an image view.
	 */
	public static class Builder {
		private static final VkComponentMapping DEFAULT_MAPPING = new VkComponentMapping();

		static {
			final VkComponentSwizzle identity = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY;
			DEFAULT_MAPPING.r = identity;
			DEFAULT_MAPPING.g = identity;
			DEFAULT_MAPPING.b = identity;
			DEFAULT_MAPPING.a = identity;
		}

		private final VkImageViewCreateInfo info = new VkImageViewCreateInfo();
		private Image image;

		/**
		 * Constructor.
		 */
		public Builder() {
			type(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D);
			mapping(DEFAULT_MAPPING);
		}

		/**
		 * Sets the image for this view.
		 * @param image Image
		 */
		public Builder image(Image image) {
			this.image = notNull(image);
			this.info.image = image.handle();
			this.info.format = image.format();
			this.info.subresourceRange = range(image.aspect());
			return this;
		}

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			info.viewType = notNull(type);
			return this;
		}

		/**
		 * Sets the RGBA swizzle mapping.
		 * @param mapping Component mapping
		 */
		public Builder mapping(VkComponentMapping mapping) {
			info.components = notNull(mapping);
			return this;
		}

		/**
		 * Sets the sub-resource range of this view.
		 * @param range Sub-resource range
		 */
		public Builder range(VkImageSubresourceRange range) {
			info.subresourceRange = notNull(range);
			return this;
		}

		/**
		 * @return Default image sub-resource descriptor
		 */
		private static VkImageSubresourceRange range(Set<VkImageAspectFlag> aspect) {
			final VkImageSubresourceRange range = new VkImageSubresourceRange();
			range.aspectMask = IntegerEnumeration.mask(aspect);
			range.baseMipLevel = 0;
			range.levelCount = 1;
			range.baseArrayLayer = 0;
			range.layerCount = 1;
			return range;
		}
		// TODO - builder for these?

		/**
		 * Constructs this image view.
		 * @return New image view
		 * @throws IllegalArgumentException if the image has not been set
		 */
		public View build() {
			// Validate
			if(image == null) throw new IllegalArgumentException("Image not populated");

			// Allocate image view
			final LogicalDevice dev = image.device();
			final VulkanLibrary lib = dev.library();
			final PointerByReference view = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create image view
			return new View(view.getValue(), image);
		}
	}
}
