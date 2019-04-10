package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.texture.Sampler;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * TODO
 * @author Sarge
 * TODO - no destroy for swap-chain images
 */
public class ImageView extends LogicalDeviceHandle {
	/**
	 * Creates a simple view of the given image.
	 * @param image Image
	 * @return Image view
	 */
	public static ImageView create(VulkanImage image) {
		return new Builder(image.device(), image).build();
	}

	private final VulkanImage image;

	/**
	 * Constructor.
	 * @param handle Image view handle
	 */
	ImageView(Pointer handle, LogicalDevice dev, VulkanImage image) {
		super(handle, dev, lib -> lib::vkDestroyImageView);
		this.image = notNull(image);
	}

	/**
	 * @return Underlying image
	 */
	public VulkanImage image() {
		return image;
	}

	/**
	 * Vulkan implementation.
	 */
	public class VulkanSampler extends LogicalDeviceHandle implements Sampler {
		private VulkanSampler(Pointer handle) {
			super(handle, ImageView.this.device(), lib -> lib::vkDestroySampler);
		}

		/**
		 * @return Image-view used by this sampler
		 */
		public ImageView view() {
			return ImageView.this;
		}
	}

	/**
	 * @param descriptor Sampler descriptor
	 * @return New sampler
	 */
	public VulkanSampler sampler(Sampler.Descriptor descriptor) {
		// Init descriptor
		final VkSamplerCreateInfo info = new VkSamplerCreateInfo();
		info.unnormalizedCoordinates = VulkanBoolean.FALSE;
		info.compareEnable = VulkanBoolean.FALSE;
		info.compareOp = VkCompareOp.VK_COMPARE_OP_ALWAYS;

		// Init minification/magnification filters
		info.minFilter = filter(descriptor.min());
		info.magFilter = filter(descriptor.mag());

		// Init addressing mode
		// TODO - separate for each dimension
		final VkSamplerAddressMode wrap = wrap(descriptor.wrap(), descriptor.isMirrored());
		info.addressModeU = wrap;
		info.addressModeV = wrap;
		info.addressModeW = wrap;
		info.borderColor = border(descriptor.border());

		// Init anisotrophy filtering
		final int anisotrophy = descriptor.anisotrophy();
		info.anisotropyEnable = VulkanBoolean.of(anisotrophy > 0);
		info.maxAnisotropy = anisotrophy; 				// TODO - check vs device props

		// Init mip-mapping
		info.mipmapMode = mipmap(descriptor.mipmap());
		// TODO
		info.mipLodBias = 0;
		info.minLod = 0;
		info.maxLod = 0;

		// Create sampler
		final Vulkan vulkan = super.vulkan();
		final PointerByReference sampler = vulkan.factory().reference();
		check(vulkan.library().vkCreateSampler(device().handle(), info, null, sampler));
		return new VulkanSampler(sampler.getValue());
	}

	/**
	 * Maps sampler filters.
	 */
	private static VkFilter filter(Sampler.Descriptor.Filter filter) {
		switch(filter) {
		case LINEAR:		return VkFilter.VK_FILTER_LINEAR;
		case NEAREST:		return VkFilter.VK_FILTER_NEAREST;
		default:			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Maps sampler address mode.
	 */
	private static VkSamplerAddressMode wrap(Sampler.Descriptor.Wrap wrap, boolean mirror) {
		switch(wrap) {
		case REPEAT:		return mirror ? VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT : VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
		case EDGE:			return mirror ? VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE : VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
		case BORDER:		return VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
		default:			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Maps sampler border colour.
	 */
	private static VkBorderColor border(Sampler.Descriptor.Border border) {
		switch(border) {
		case BLACK:			return VkBorderColor.VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK;
		case WHITE:			return VkBorderColor.VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE;
		case TRANSPARENT:	return VkBorderColor.VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK;
		default:			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Maps sampler mip-map filter.
	 */
	private static VkSamplerMipmapMode mipmap(Sampler.Descriptor.Filter filter) {
		switch(filter) {
		case LINEAR:		return VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR;
		case NEAREST:		return VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR;
		default:			throw new UnsupportedOperationException();
		}
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
			info.subresourceRange = range(image.aspect());
		}

		/**
		 * @return Default component mapping
		 * TODO - add as ctor / static factory on actual struct class? i.e. is this used elsewhere?
		 * 		- would prevent more errors since can then assume structs are valid?
		 */
		private static VkComponentMapping mapping() {
			// VulkanHelper.colourComponent(components)
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
		private static VkImageSubresourceRange range(Set<VkImageAspectFlag> aspect) {
			// TODO - is this not the same as the one we use to create the image?
			final VkImageSubresourceRange range = new VkImageSubresourceRange();
			range.aspectMask = IntegerEnumeration.mask(aspect); //VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value();
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
			// Allocate image view
			final Vulkan vulkan = dev.vulkan();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference view = vulkan.factory().reference();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create image view
			return new ImageView(view.getValue(), dev, image);
		}
	}
}
