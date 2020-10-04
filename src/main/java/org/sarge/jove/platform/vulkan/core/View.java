package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> TODO
 * @author Sarge
 */
public class View extends AbstractVulkanObject {
	private final Image.Descriptor descriptor;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param image		Image descriptor
	 * @param dev		Logical device
	 */
	private View(Pointer handle, Image.Descriptor descriptor, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyImageView);
		this.descriptor = notNull(descriptor);
	}

	/**
	 * @return Descriptor for the underlying image
	 */
	public Image.Descriptor descriptor() {
		return descriptor;
	}

	/**
	 * Builder for an image view.
	 */
	public static class Builder {
		private static final VkComponentMapping DEFAULT_COMPONENT_MAPPING = create();

		private static VkComponentMapping create() {
			final VkComponentSwizzle identity = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY;
			final var mapping = new VkComponentMapping();
			mapping.r = identity;
			mapping.g = identity;
			mapping.b = identity;
			mapping.a = identity;
			return mapping;
		}

		private final LogicalDevice dev;
		private Image.Descriptor descriptor;
		private VkImageViewType type;
		private VkComponentMapping mapping = DEFAULT_COMPONENT_MAPPING;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Sets the image descriptor for this view.
		 * @param descriptor Image descriptor
		 */
		public Builder image(Image.Descriptor descriptor) {
			this.descriptor = notNull(descriptor);
			return this;
		}

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			this.type = notNull(type);
			return this;
		}

		/**
		 * Sets the RGBA swizzle mapping.
		 * @param mapping Component mapping
		 */
		public Builder mapping(VkComponentMapping mapping) {
			this.mapping = notNull(mapping);
			return this;
		}

// TODO - not sure how this is used
//		/**
//		 * Sets the sub-resource range of this view.
//		 * @param range Sub-resource range
//		 */
//		public Builder range(VkImageSubresourceRange range) {
//			info.subresourceRange = notNull(range);
//			return this;
//		}

		/**
		 * @return Default image sub-resource descriptor
		 */
		private static VkImageSubresourceRange range(Set<VkImageAspectFlag> aspect) {
			final VkImageSubresourceRange range = new VkImageSubresourceRange();
			range.aspectMask = IntegerEnumeration.mask(aspect);
			// TODO - following initialised from image descriptor?
			range.baseMipLevel = 0;
			range.levelCount = 1;
			range.baseArrayLayer = 0;
			range.layerCount = 1;
			return range;
		}
		// TODO - builder for this?

		/**
		 * Constructs this image view.
		 * @return New image view
		 * @throws IllegalArgumentException if the image descriptor has not been specified
		 */
		public View build() {
			// Validate
			if(descriptor == null) throw new IllegalArgumentException("Image descriptor not specified");

			// Init view type if not explicitly specified
			if(type == null) {
				type = switch(descriptor.type()) {
					case VK_IMAGE_TYPE_1D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_1D;
					case VK_IMAGE_TYPE_2D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_2D;
					case VK_IMAGE_TYPE_3D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_3D;
				};
			}

			// Build view descriptor
			final VkImageViewCreateInfo info = new VkImageViewCreateInfo();
			info.viewType = type;
			info.format = descriptor.format();
			info.image = descriptor.handle();
			info.components = mapping;
			info.subresourceRange = range(descriptor.aspect());

			// Allocate image view
			final VulkanLibrary lib = dev.library();
			final PointerByReference view = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create image view
			return new View(view.getValue(), descriptor, dev);
		}
	}
}
