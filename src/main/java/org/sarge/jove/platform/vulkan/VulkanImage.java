package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.texture.Image;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan image</i> stores image data.
 * @author Sarge
 */
public class VulkanImage extends LogicalDeviceHandle {
	/**
	 * Helper - Builds image extents.
	 * @param extents Extents array
	 * @return Image extents
	 * @throws IllegalArgumentException if the number of extents is not 1..3
	 * @throws IllegalArgumentException if any extent is not one-or-more
	 */
	public static VkExtent3D extents(int... extents) {
		// Init extents
		final VkExtent3D result = new VkExtent3D();
		result.depth = 1;
		result.height = 1;

		// Populate extents from array
		switch(extents.length) {
		case 3:
			result.depth = oneOrMore(extents[2]);
			//$FALL-THROUGH$

		case 2:
			result.height = oneOrMore(extents[1]);
			//$FALL-THROUGH$

		case 1:
			result.width = oneOrMore(extents[0]);
			break;

		default:
			throw new IllegalArgumentException("Invalid extents");
		}

		return result;
	}

	/**
	 * Helper - Clones the given image extents.
	 * @param extents Extents
	 * @return Clone
	 */
	public static VkExtent3D clone(VkExtent3D extents) {
		final VkExtent3D clone = new VkExtent3D();
		clone.width = extents.width;
		clone.height = extents.height;
		clone.depth = extents.depth;
		return clone;
	}

	private final VkFormat format;
	private final VkExtent3D extents;

	/**
	 * Constructor.
	 * @param handle		Image handle
	 * @param dev			Logical device
	 * @param format		Image format
	 * @param extents		Image extents
	 */
	public VulkanImage(Pointer handle, LogicalDevice dev, VkFormat format, VkExtent3D extents) {
		super(handle, dev, lib -> lib::vkDestroyImage); // TODO - not required for swap-chain images
		this.format = notNull(format);
		this.extents = clone(extents);
	}

	/**
	 * @return Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Image extents
	 */
	public VkExtent3D extents() {
		return clone(extents);
	}

	/**
	 * Builder for a Vulkan image.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final VkImageCreateInfo info = new VkImageCreateInfo();
		private final Set<VkMemoryPropertyFlag> props = new StrictSet<>();

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			init();
		}

		/**
		 * Initialises the image descriptor.
		 */
		private void init() {
			type(VkImageType.VK_IMAGE_TYPE_2D);
			mipLevels(1);
			arrayLayers(1);
			tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL);
			initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
			samples(VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT);
			mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
		}

		/**
		 * Initialises the format and extents of this image.
		 * @param image Image
		 */
		public Builder image(Image image) {
			final VkFormat format = format(image);
			format(format);
			extents(image.header().size());
			return this;
		}

		/**
		 * Determines the Vulkan format for the given image.
		 * @param image Image
		 * @return Image format
		 */
		private static VkFormat format(Image image) {
			// TODO - assumes RGBA bytes
			return new VulkanHelper.FormatBuilder()
				.bytes(1)
				.signed(false)
				.type(Vertex.Component.Type.NORM)
				.build();
		}

		/**
		 * Sets the type of this image.
		 * @param type Image type
		 */
		public Builder type(VkImageType type) {
			info.imageType = notNull(type);
			return this;
		}

		/**
		 * Sets the extents of this image.
		 * @param extents Image extents
		 */
		public Builder extents(VkExtent3D extents) {
			info.extent = VulkanImage.clone(extents);
			return this;
		}

		/**
		 * Sets the extents of a 2D image.
		 * @param extents Image extents
		 */
		public Builder extents(Dimensions dim) {
			info.extent = VulkanImage.extents(dim.width, dim.height);
			return this;
		}

		/**
		 * Sets the number of mipmap levels.
		 * @param mipLevels Mipmap levels
		 */
		public Builder mipLevels(int mipLevels) {
			info.mipLevels = oneOrMore(mipLevels);
			return this;
		}

		/**
		 * Sets the number of layers.
		 * @param layers Layers
		 */
		public Builder arrayLayers(int layers) {
			info.arrayLayers = oneOrMore(layers);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Format
		 */
		public Builder format(VkFormat format) {
			info.format = notNull(format);
			// TODO - check format supported by device
			return this;
		}

		/**
		 * Sets the image tiling.
		 * @param tiling Tiling
		 */
		public Builder tiling(VkImageTiling tiling) {
			info.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Sets the initial layout of this image.
		 * @param layout Initial layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			info.initialLayout = notNull(layout);
			return this;
		}

		/**
		 * Adds a usage flag.
		 * @param usage Usage
		 */
		public Builder usage(VkImageUsageFlag usage) {
			info.usage |= usage.value();
			return this;
		}

		/**
		 * Sets the number of samples for this image.
		 * @param samples Samples
		 */
		public Builder samples(VkSampleCountFlag samples) {
			info.samples = samples.value();
			return this;
		}

		/**
		 * Sets the sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			info.sharingMode = notNull(mode);
			return this;
		}

		/**
		 * Adds a memory property for this image.
		 * @param prop Memory property
		 */
		public Builder property(VkMemoryPropertyFlag prop) {
			props.add(prop);
			return this;
		}

		/**
		 * Constructs this image.
		 * @return New image
		 */
		public VulkanImage build() {
			// Validate
			if(info.extent == null) throw new IllegalArgumentException("Image extents not specified");
			if(info.format == null) throw new IllegalArgumentException("Image format not specified");
			// TODO - check format supported by device

			// Allocate image
			final PhysicalDevice parent = dev.parent();
			final Vulkan vulkan = parent.vulkan();
			final VulkanLibraryImage lib = vulkan.library();
			final PointerByReference handle = vulkan.factory().reference();
			check(lib.vkCreateImage(dev.handle(), info, null, handle));

			// Determine memory requirements
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev.handle(), handle.getValue(), reqs);

			// Allocate image memory
			final Pointer mem = dev.allocate(reqs, props);

			// Bind image memory
			check(lib.vkBindImageMemory(dev.handle(), handle.getValue(), mem, 0L));

			// Create image
			return new VulkanImage(handle.getValue(), dev, info.format, info.extent);
		}
	}
}
