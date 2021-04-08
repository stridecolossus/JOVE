package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor.SubResourceBuilder;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> is a reference to an {@link Image}.
 * @author Sarge
 */
public class View extends AbstractVulkanObject {
	/**
	 * Helper - Maps an image type to the corresponding view type.
	 * @param type Image type
	 * @return View type
	 */
	public static VkImageViewType type(VkImageType type) {
		return switch(type) {
			case VK_IMAGE_TYPE_1D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_1D;
			case VK_IMAGE_TYPE_2D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_2D;
			case VK_IMAGE_TYPE_3D -> VkImageViewType.VK_IMAGE_VIEW_TYPE_3D;
		};
	}

	private final Image image;

	private ClearValue clear = ClearValue.NONE;

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
	 * Helper.
	 * @return Image extents
	 * @see Image.Descriptor#extents()
	 */
	public final Image.Extents extents() {
		return image().descriptor().extents();
	}

	/**
	 * Clear value for this attachment.
	 * @return Clear value
	 */
	public ClearValue clear() {
		return clear;
	}

	/**
	 * Sets the clear value for this attachment.
	 * @param aspect		Expected image aspect
	 * @param clear			Clear value
	 * @throws IllegalArgumentException if the clear value is incompatible with this view
	 */
	public void clear(ClearValue clear) {
		if(clear != ClearValue.NONE) {
			final var aspects = image.descriptor().aspects();
			if(!aspects.contains(clear.aspect())) {
				throw new IllegalArgumentException(String.format("Invalid clear value for this view: clear=%s view=%s", clear, this));
			}
		}
		this.clear = notNull(clear);
	}

	@Override
	protected void release() {
		if(image instanceof AbstractTransientNativeObject obj) {
			obj.destroy();
		}
		super.release();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("image", image)
				.append("clear", clear)
				.build();
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
		private final Image image;
		private final SubResourceBuilder<Builder> subresource;

		private VkImageViewType type;
		private VkComponentMapping mapping = DEFAULT_COMPONENT_MAPPING;
		private ClearValue clear;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev, Image image) {
			this.dev = notNull(dev);
			this.image = notNull(image);
			this.subresource = image.descriptor().builder(this);
			this.clear = clear(image.descriptor());
		}

		/**
		 * @return Default clear value for the given image
		 */
		private static ClearValue clear(Image.Descriptor descriptor) {
			if(descriptor.aspects().contains(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)) {
				return ClearValue.DEPTH;
			}
			else {
				return ClearValue.NONE;
			}
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
		 * Sets the clear value for this view.
		 * @param clear Clear value
		 */
		public Builder clear(ClearValue clear) {
			this.clear = notNull(clear);
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
		// TODO - use this and remove swizzle in image
		// TODO - no need to init since all zeroes?

		/**
		 * @return Sub-resource builder for this view
		 */
		public SubResourceBuilder<Builder> subresource() {
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
				type = View.type(image.descriptor().type());
			}

			// Build view descriptor
			final VkImageViewCreateInfo info = new VkImageViewCreateInfo();
			info.viewType = type;
			info.format = image.descriptor().format();
			info.image = image.handle();
			info.components = mapping;
			subresource.populate(info.subresourceRange);

			// Allocate image view
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, handle));

			// Create image view
			final View view = new View(handle.getValue(), image, dev);

			// Init clear value
			view.clear(clear);

			return view;
		}
	}
}
