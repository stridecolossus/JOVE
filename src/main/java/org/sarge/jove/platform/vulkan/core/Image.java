package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image</i> is a texture or data image stored on the hardware.
 * @author Sarge
 */
public interface Image extends NativeObject {
	/**
	 * @return Descriptor for this image
	 */
	Descriptor descriptor();

	/**
	 * Image extents.
	 */
	record Extents(int width, int height, int depth) {
		/**
		 * Creates a 2D image extents from the given dimensions.
		 * @param dim Image dimensions
		 * @return New extents
		 */
		public static Extents of(Dimensions dim) {
			return new Extents(dim.width(), dim.height());
		}

		/**
		 * Constructor.
		 * @param width
		 * @param height
		 * @param depth
		 */
		public Extents {
			Check.oneOrMore(width);
			Check.zeroOrMore(height);
			Check.oneOrMore(depth);
		}

		/**
		 * Constructor for 2D extents.
		 * @param width
		 * @param height
		 */
		public Extents(int width, int height) {
			this(width, height, 1);
		}

		/**
		 * @return New Vulkan descriptor for this image extents
		 */
		public VkExtent3D create() {
			final VkExtent3D extent = new VkExtent3D();
			extent.width = width;
			extent.height = height;
			extent.depth = depth;
			return extent;
		}
	}

	/**
	 * Descriptor for an image.
	 */
	record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspects) {
		/**
		 * Constructor.
		 */
		public Descriptor {
			Check.notNull(type);
			Check.notNull(format);
			Check.notNull(extents);
			Check.notNull(aspects);
		}

		/**
		 * Builder for an image descriptor.
		 */
		public static class Builder {
			private VkImageType type = VkImageType.VK_IMAGE_TYPE_2D;
			private VkFormat format;
			private Extents extents;
			private final Set<VkImageAspectFlag> aspects = new HashSet<>();

			/**
			 * Sets the image type.
			 * @param type Image type (default is {@link VkImageType#VK_IMAGE_TYPE_2D})
			 */
			public Builder type(VkImageType type) {
				this.type = notNull(type);
				return this;
			}

			/**
			 * Sets the image format.
			 * @param format Image format
			 */
			public Builder format(VkFormat format) {
				this.format = notNull(format);
				return this;
			}

			/**
			 * Sets the image extents.
			 * @param extents Image extents
			 */
			public Builder extents(Extents extents) {
				this.extents = notNull(extents);
				return this;
			}

			/**
			 * Adds an image aspect.
			 * @param aspect Image aspect
			 */
			public Builder aspect(VkImageAspectFlag aspect) {
				Check.notNull(aspect);
				aspects.add(aspect);
				return this;
			}

			/**
			 * Constructs this descriptor.
			 * @return New image descriptor
			 * @throws IllegalArgumentException if the format or extents are not specified
			 */
			public Descriptor build() {
				Check.notNull(format);
				Check.notNull(extents);
				return new Descriptor(type, format, extents, aspects);
			}
		}
	}

	/**
	 * Default implementation.
	 */
	class DefaultImage extends AbstractVulkanObject implements Image {
		private final Descriptor descriptor;
		private final Pointer mem;

		/**
		 * Constructor.
		 * @param handle		Handle
		 * @param descriptor	Image descriptor
		 * @param mem			Internal memory
		 * @param dev			Logical device
		 */
		protected DefaultImage(Handle handle, Descriptor descriptor, Pointer mem, LogicalDevice dev) {
			super(handle, dev, dev.library()::vkDestroyImage);
			this.descriptor = notNull(descriptor);
			this.mem = notNull(mem);
		}

		@Override
		public Descriptor descriptor() {
			return descriptor;
		}

		@Override
		public synchronized void destroy() {
			final LogicalDevice dev = this.device();
			dev.library().vkFreeMemory(dev.handle(), mem, null);
			super.destroy();
		}
	}

	/**
	 * Builder for an image.
	 */
	class Builder {
		private final LogicalDevice dev;
		private final VkImageCreateInfo info = new VkImageCreateInfo();
		private final Set<VkImageUsageFlag> usage = new HashSet<>();
		private final Set<VkMemoryPropertyFlag> props = new HashSet<>();
		private final Set<VkImageAspectFlag> aspects = new HashSet<>();
		private Extents extents;

		/**
		 * Constructor.
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
			samples(VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT);
			tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL);
			mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
			initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
		}

		/**
		 * Sets the image type.
		 * @param type Image type
		 */
		public Builder type(VkImageType type) {
			info.imageType = notNull(type);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Image format
		 */
		public Builder format(VkFormat format) {
			info.format = notNull(format);
			return this;
		}

		/**
		 * Sets the image extents.
		 * @param extents Image extents
		 */
		public Builder extents(Extents extents) {
			this.extents = notNull(extents);
			return this;
		}

		/**
		 * Sets the number of mip levels.
		 * @param mipLevels Mip-levels (default is {@code 1})
		 */
		public Builder mipLevels(int mipLevels) {
			info.mipLevels = oneOrMore(mipLevels);
			return this;
		}

		/**
		 * Sets the number of array layers.
		 * @param arrayLayers Number of array layers (default is {@code 1})
		 */
		public  Builder arrayLayers(int arrayLayers) {
			info.arrayLayers = oneOrMore(arrayLayers);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Samples-per-texel (default is {@code 1})
		 */
		public Builder samples(VkSampleCountFlag samples) {
			info.samples = notNull(samples);
			return this;
		}

		/**
		 * Sets the image tiling arrangement.
		 * @param tiling Tiling arrangement (default is {@link VkImageTiling#VK_IMAGE_TILING_OPTIMAL})
		 */
		public Builder tiling(VkImageTiling tiling) {
			info.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Adds an usage flag.
		 * @param usage Usage flag
		 */
		public Builder usage(VkImageUsageFlag usage) {
			Check.notNull(usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the sharing mode of the image.
		 * @param mode Sharing mode (default is {@link VkSharingMode#VK_SHARING_MODE_EXCLUSIVE})
		 */
		public Builder mode(VkSharingMode mode) {
			info.sharingMode = notNull(mode);
			return this;
		}
		// TODO - queue families/count if concurrent

		/**
		 * Sets the initial image layout.
		 * @param initialLayout Initial layout (default is undefined)
		 */
		public Builder initialLayout(VkImageLayout initialLayout) {
			info.initialLayout = notNull(initialLayout);
			// TODO - undefined or pre-init only
			return this;
		}

		/**
		 * Adds a memory property.
		 * @param prop Memory property
		 */
		public Builder property(VkMemoryPropertyFlag prop) {
			Check.notNull(prop);
			props.add(prop);
			return this;
		}

		/**
		 * Adds an image aspect.
		 * @param aspect Image aspect
		 */
		public Builder aspect(VkImageAspectFlag aspect) {
			Check.notNull(aspect);
			aspects.add(aspect);
			return this;
		}

		/**
		 * Constructs this image.
		 * @return New image
		 */
		public Image build() {
			// Validate image
			if(info.format == null) throw new IllegalArgumentException("Image format not specified");
			if(extents == null) throw new IllegalArgumentException("Image extents not specified");
			// TODO - aspects not empty?

			// Complete create descriptor
			info.extent = this.extents.create();
			info.usage = IntegerEnumeration.mask(usage);

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = lib.factory().pointer();
			check(lib.vkCreateImage(dev.handle(), info, null, ref));

			// Create image descriptor
			final Handle handle = new Handle(ref.getValue());
			final Descriptor descriptor = new Descriptor(info.imageType, info.format, extents, aspects);

			// Retrieve image memory requirements
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev.handle(), handle, reqs);

			// Allocate image memory
			final Pointer mem = dev.allocate(reqs, props);
			check(lib.vkBindImageMemory(dev.handle(), handle, mem, 0));

			// Create image
			return new DefaultImage(handle, descriptor, mem, dev);
		}
	}
}
