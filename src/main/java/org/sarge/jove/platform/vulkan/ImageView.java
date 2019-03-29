package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import com.sun.jna.ptr.PointerByReference;

/**
 * TODO
 * @author Sarge
 * TODO - no destroy for swap-chain images
 */
public class ImageView extends VulkanHandle {
	/**
	 * Constructor.
	 * @param handle Image view handle
	 */
	ImageView(VulkanHandle handle) {
		super(handle);
	}

	/**
	 * Builder for an image view.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final VulkanImage image;
		private final VkImageViewCreateInfo info = new VkImageViewCreateInfo();

		/**
		 * Constructor.
		 * @param dev		Logical device that owns this image view
		 * @param image 	Vulkan image
		 */
		public Builder(LogicalDevice dev, VulkanImage image) {
			this.dev = notNull(dev);
			this.image = notNull(image);
			init();
		}

		/**
		 * Initialises the image view descriptor.
		 */
		private void init() {
			info.image = image.handle();
			info.viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D;
			info.format = image.format();
			info.components = mapping();
			info.subresourceRange = range();
		}

		/**
		 * @return Default component mapping
		 * TODO - add as ctor / static factory on actual struct class? i.e. is this used elsewhere?
		 * 		- would prevent more errors since can then assume structs are valid?
		 */
		private static VkComponentMapping mapping() {
			final VkComponentSwizzle identity = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY;
			final VkComponentMapping mapping = new VkComponentMapping();
			mapping.r = identity;
			mapping.g = identity;
			mapping.b = identity;
			mapping.a = identity;
			return mapping;
		}

		/**
		 * @return Default image sub-resource descriptor
		 */
		private static VkImageSubresourceRange range() {
			final VkImageSubresourceRange range = new VkImageSubresourceRange();
			range.aspectMask = VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value();
			range.baseMipLevel = 0;
			range.levelCount = 1;
			range.baseArrayLayer = 0;
			range.layerCount = 1;
			return range;
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
		 * Constructs this image view.
		 * @return New image view
		 */
		public ImageView build() {
			// Create image view
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference view = vulkan.factory().reference();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create wrapper
			Destructor destructor = () -> lib.vkDestroyImageView(dev.handle(), view.getValue(), null);
			return new ImageView(new VulkanHandle(view.getValue(), destructor));
		}
	}
}
