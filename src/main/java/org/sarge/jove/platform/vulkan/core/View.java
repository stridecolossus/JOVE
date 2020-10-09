package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.ImageSubResourceBuilder;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> is a reference to an {@link Image}.
 * @author Sarge
 */
public class View extends AbstractVulkanObject {
	private final Image image;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param image		Image
	 * @param dev		Logical device
	 */
	View(Pointer handle, Image image, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyImageView);
		this.image = notNull(image);
	}

	/**
	 * @return Underlying image
	 */
	public Image image() {
		return image;
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
		private Image image;
		private VkImageViewType type;
		private VkComponentMapping mapping = DEFAULT_COMPONENT_MAPPING;
		private final ImageSubResourceBuilder<Builder> subresource = new ImageSubResourceBuilder<>(this);

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
		public Builder image(Image image) {
			this.image = notNull(image);
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

		/**
		 * @return Image sub-resource range builder
		 */
		public ImageSubResourceBuilder<Builder> subresource() {
			return subresource;
		}

		/**
		 * Constructs this image view.
		 * @return New image view
		 * @throws IllegalArgumentException if the image descriptor has not been specified
		 */
		public View build() {
			// Validate
			if(image == null) throw new IllegalArgumentException("Image descriptor not specified");

			// Init view type if not explicitly specified
			if(type == null) {
				type = switch(image.descriptor().type()) {
					case VK_IMAGE_TYPE_1D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_1D;
					case VK_IMAGE_TYPE_2D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_2D;
					case VK_IMAGE_TYPE_3D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_3D;
				};
			}

			// Build view descriptor
			final VkImageViewCreateInfo info = new VkImageViewCreateInfo();
			info.viewType = type;
			info.format = image.descriptor().format();
			info.image = image.handle();
			info.components = mapping;
			info.subresourceRange = subresource.range();

			// Allocate image view
			final VulkanLibrary lib = dev.library();
			final PointerByReference view = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create image view
			return new View(view.getValue(), image, dev);
		}
	}
}
