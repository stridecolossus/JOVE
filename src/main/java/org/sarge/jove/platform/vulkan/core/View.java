package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
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
	static VkImageViewType type(VkImageType type) {
		return switch(type) {
			case IMAGE_TYPE_1D -> VkImageViewType.VIEW_TYPE_1D;
			case IMAGE_TYPE_2D -> VkImageViewType.VIEW_TYPE_2D;
			case IMAGE_TYPE_3D -> VkImageViewType.VIEW_TYPE_3D;
		};
	}

	private final Image image;

	private ClearValue clear;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param image		Image
	 * @param dev		Logical device
	 */
	View(Pointer handle, Image image, DeviceContext dev) {
		super(handle, dev);
		this.image = notNull(image);
		this.clear = clear(image);
	}

	/**
	 * @return Default clear value for the given image
	 */
	private static ClearValue clear(Image image) {
		if(image.descriptor().aspects().contains(VkImageAspect.DEPTH)) {
			return ClearValue.DEPTH;
		}
		else {
			return ClearValue.NONE;
		}
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
	protected Destructor destructor(VulkanLibrary lib) {
		return lib::vkDestroyImageView;
	}

	@Override
	protected void release() {
		if(image instanceof AbstractTransientNativeObject obj) {
			obj.destroy();
		}
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
			final VkComponentSwizzle identity = VkComponentSwizzle.IDENTITY;
			final var mapping = new VkComponentMapping();
			mapping.r = identity;
			mapping.g = identity;
			mapping.b = identity;
			mapping.a = identity;
			return mapping;
		}

		// TODO - this seems a bit arse-backwards, also image should be passed into build(), messy having to pass in ctor and keep ref just for the sub-resource
		private final Image image;
		private final SubResourceBuilder<Builder> subresource;

		private VkImageViewType type;
		private VkComponentMapping mapping = DEFAULT_COMPONENT_MAPPING;

		/**
		 * Constructor.
		 * @param image Image
		 */
		Builder(Image image) {
			this.image = notNull(image);
			this.subresource = image.descriptor().builder(this);
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
			final DeviceContext dev = image.device();
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, handle));

			// Create image view
			return new View(handle.getValue(), image, dev);
		}
	}
}
