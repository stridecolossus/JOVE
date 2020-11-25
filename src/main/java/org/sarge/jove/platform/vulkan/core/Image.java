package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanException;
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
		 * Helper - Populates a Vulkan rectangle.
		 * @param in		Rectangle
		 * @param out		Vulkan rectangle
		 */
		public static void populate(Rectangle in, VkRect2D out) {
			out.offset.x = in.x();
			out.offset.y = in.y();
			out.extent.width = in.width();
			out.extent.height = in.height();
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
		 * Populates a Vulkan rectangle from this extents.
		 * @param rect Rectangle to populate
		 */
		public void populate(VkRect2D rect) {
			rect.extent.width = width;
			rect.extent.height = height;
		}

		/**
		 * Populate a Vulkan 3D extent from this extents.
		 * @param extent Extents to populate
		 */
		public void populate(VkExtent3D extent) {
			extent.width = width;
			extent.height = height;
			extent.depth = depth;
		}
	}

	/**
	 * Descriptor for an image.
	 */
	record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspects) {
		/**
		 * Constructor.
		 * @param type			Image type
		 * @param format		Format
		 * @param extents		Extents
		 * @param aspects		Image aspect(s)
		 * @throws IllegalArgumentException unless at least one image aspect has been specified
		 * @throws IllegalArgumentException if the combination of image aspects is not valid
		 * @throws IllegalArgumentException if the extents are invalid for the given image type
		 */
		public Descriptor {
			this.type = notNull(type);
			this.format = notNull(format);
			this.extents = notNull(extents);
			this.aspects = Set.copyOf(aspects);
			validateTypeExtents();
			validateAspects();
			// TODO - validate format against aspects, e.g. D32_FLOAT is not stencil, D32_FLOAT_S8_UINT has stencil
		}

		private void validateTypeExtents() {
			final boolean valid = switch(type) {
				case VK_IMAGE_TYPE_1D -> (extents.height == 1) && (extents.depth == 1);
				case VK_IMAGE_TYPE_2D -> extents.depth == 1;
				case VK_IMAGE_TYPE_3D -> true; // TODO - layers = 1
			};
			if(!valid) {
				throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
			}
		}

		private static final Collection<Set<VkImageAspectFlag>> VALID_ASPECTS = List.of(
				Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT),
				Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT),
				Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, VkImageAspectFlag.VK_IMAGE_ASPECT_STENCIL_BIT)
		);

		private void validateAspects() {
			if(aspects.isEmpty()) throw new IllegalArgumentException("Image must have at least one aspect");
			if(!VALID_ASPECTS.contains(aspects)) throw new IllegalArgumentException("Invalid image aspects: " + aspects);
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
		private final Set<VkImageAspectFlag> aspects = new HashSet<>();
		private final MemoryAllocator.Allocation allocation;
		private Extents extents;

		/**
		 * Constructor.
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			this.allocation = dev.allocator().allocation();
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
		public Builder arrayLayers(int arrayLayers) {
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
			allocation.property(prop);
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
		 * @throws VulkanException if the image cannot be created
		 * @throws IllegalArgumentException if the number of array layers is not one for a {@link VkImageType#VK_IMAGE_TYPE_3D} image
		 * @see Descriptor#Descriptor(VkImageType, VkFormat, Extents, Set)
		 */
		public Image build() {
			// Validate
			if(info.format == null) throw new IllegalArgumentException("No image format specified");
			if(extents == null) throw new IllegalArgumentException("No image extents specified");
			if((info.imageType == VkImageType.VK_IMAGE_TYPE_3D) && (info.arrayLayers != 1)) throw new IllegalArgumentException("Array layers must be one for a 3D image");

			// Create image descriptor
			final Descriptor descriptor = new Descriptor(info.imageType, info.format, extents, aspects);

			// Complete create descriptor
			extents.populate(info.extent);
			info.usage = IntegerEnumeration.mask(usage);

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = lib.factory().pointer();
			check(lib.vkCreateImage(dev.handle(), info, null, ref));

			// Create image descriptor
			final Handle handle = new Handle(ref.getValue());

			// Retrieve image memory requirements
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev.handle(), handle, reqs);

			// Allocate image memory
			final Pointer mem = allocation.init(reqs).allocate();
			check(lib.vkBindImageMemory(dev.handle(), handle, mem, 0));

			// Create image
			return new DefaultImage(handle, descriptor, mem, dev);
		}
	}
}
