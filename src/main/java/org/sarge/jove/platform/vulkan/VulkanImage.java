package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan image</i> stores image data.
 * @author Sarge
 */
public class VulkanImage extends LogicalDeviceHandle {
	private final VkFormat format;
	private final VkExtent3D extent;

	/**
	 * Constructor.
	 * @param handle		Image handle
	 * @param dev			Logical device
	 * @param format		Image format
	 * @param extent		Extents
	 */
	public VulkanImage(Pointer handle, LogicalDevice dev, VkFormat format, VkExtent3D extent) {
		super(handle, dev, lib -> lib::vkDestroyImage); // TODO - no required for swap-chain images
		this.format = notNull(format);
		this.extent = notNull(extent);
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
	public VkExtent3D extent() {
		return extent;
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
		 * @throws IllegalArgumentException if the number of extents is not 1..3
		 */
		public Builder extents(int... extents) {
			Check.range(extents.length, 1, 3);

			// Init width
			info.extent = new VkExtent3D();
			info.extent.width = extents[0];

			// Add height for 2D
			if(extents.length > 1) {
				info.extent.height = extents[1];
			}

			// Add depth for 3D
			if(extents.length > 2) {
				info.extent.depth = extents[2];
			}
			else {
				info.extent.depth = 1;
			}

			return this;
		}

		/**
		 * Sets the extents of a 2D image.
		 * @param extents Image extents
		 */
		public Builder extents(Dimensions extents) {
			return extents(extents.width, extents.height);
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
