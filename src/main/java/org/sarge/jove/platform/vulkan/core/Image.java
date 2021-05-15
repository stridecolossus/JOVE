package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.lib.util.Check;

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
	 * @return Device context for this image
	 */
	DeviceContext device();

	/**
	 * Creates a view for this image with default configuration.
	 * @return New image view
	 */
	default View view() {
		return new View.Builder(this).build();
	}

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
		 * Populates a Vulkan 3D extent from this object.
		 * @param extent Extents to populate
		 */
		void populate(VkExtent3D extent) {
			extent.width = width;
			extent.height = height;
			extent.depth = depth;
		}
	}

	/**
	 * Descriptor for an image.
	 */
	record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspect> aspects, int levels, int layers) {
		// Valid image aspect combinations
		private static final Collection<Set<VkImageAspect>> VALID_ASPECTS = List.of(
				Set.of(VkImageAspect.COLOR),
				Set.of(VkImageAspect.DEPTH),
				Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL)
		);

		/**
		 * Constructor.
		 * @param type			Image type
		 * @param format		Format
		 * @param extents		Extents
		 * @param aspects		Image aspect(s)
		 * @param levels		Number of mip levels
		 * @param layers		Number of array layers
		 * @throws IllegalArgumentException if the image aspects is empty or is an invalid combination
		 * @throws IllegalArgumentException if the extents are invalid for the given image type
		 */
		public Descriptor {
			// Validate
			Check.notNull(type);
			Check.notNull(format);
			Check.notNull(extents);
			Check.notEmpty(aspects);
			Check.oneOrMore(levels);
			Check.oneOrMore(layers);

			// Validate extents
			final boolean valid = switch(type) {
				case IMAGE_TYPE_1D -> (extents.height == 1) && (extents.depth == 1);
				case IMAGE_TYPE_2D -> extents.depth == 1;
				case IMAGE_TYPE_3D -> true;
			};
			if(!valid) {
				throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
			}

			// Validate array layers
			if((type == VkImageType.IMAGE_TYPE_3D) && (layers != 1)) {
				throw new IllegalArgumentException("Array layers must be one for a 3D image");
			}

			// Validate image aspects
			if(!VALID_ASPECTS.contains(aspects)) throw new IllegalArgumentException("Invalid image aspects: " + aspects);

			// TODO - validate format against aspects, e.g. D32_FLOAT is not stencil, D32_FLOAT_S8_UINT has stencil
		}

		/**
		 * Creates a nested sub-resource range builder for this image descriptor.
		 * @param <T> Parent builder type
		 * @param parent Parent builder
		 * @return New sub-resource builder
		 */
		public <T> SubResourceBuilder<T> builder(T parent) {
			return new SubResourceBuilder<>(parent);
		}

		/**
		 * Builder for an image descriptor.
		 */
		public static class Builder {
			private VkImageType type = VkImageType.IMAGE_TYPE_2D;
			private VkFormat format;
			private Extents extents;
			private final Set<VkImageAspect> aspects = new HashSet<>();
			private int levels = 1;
			private int layers = 1;

			/**
			 * Sets the image type (default is {@link VkImageType#IMAGE_TYPE_2D}).
			 * @param type Image type
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
			public Builder aspect(VkImageAspect aspect) {
				aspects.add(notNull(aspect));
				return this;
			}

			/**
			 * Sets the number of mip levels (default is one).
			 * @param levels Number of mip levels
			 */
			public Builder mipLevels(int levels) {
				this.levels = oneOrMore(levels);
				return this;
			}

			/**
			 * Sets the number of array levels (default is one).
			 * @param levels Number of array levels
			 */
			public Builder arrayLayers(int layers) {
				this.layers = oneOrMore(layers);
				return this;
			}

			/**
			 * Constructs this descriptor.
			 * @return New image descriptor
			 */
			public Descriptor build() {
				return new Descriptor(type, format, extents, aspects, levels, layers);
			}
		}

		/**
		 * Nested builder for an image sub-resource range <b>or</b> layers.
		 * <p>
		 * The aspect(s) of the sub-resource view are initialised to those of this image if not explicitly configured.
		 * <p>
		 * @param <T> Parent builder type
		 */
		public class SubResourceBuilder<T> {
			/**
			 * Special case identifier indicating the <i>remaining</i> number of mip levels or array layers.
			 */
			public static final int REMAINING = (~0);

			// TODO - remaining levels/layers
			private final T parent;
			private final Set<VkImageAspect> aspectMask = new HashSet<>();
			private int mipLevel;
			private int levelCount = 1; // REMAINING;
			private int baseArrayLayer;
			private int layerCount = 1; // REMAINING;

			private SubResourceBuilder(T parent) {
				this.parent = notNull(parent);
			}

			/**
			 * Adds an image aspect to this range.
			 * @param aspect Image aspect
			 */
			public SubResourceBuilder<T> aspect(VkImageAspect aspect) {
				aspectMask.add(notNull(aspect));
				return this;
			}

			/**
			 * Sets the mip level (or the {@code baseMipLevel} field for a {@link VkImageSubresourceRange}).
			 * @param mipLevel Mip level
			 * @throws IllegalArgumentException if the mip level exceeds that of the parent image
			 */
			public SubResourceBuilder<T> mipLevel(int mipLevel) {
				if(mipLevel > levels) {
					throw new IllegalArgumentException(String.format("Sub-resource mip level %d cannot exceed number of image mip levels %d", mipLevel, levels));
				}
				this.mipLevel = zeroOrMore(mipLevel);
				return this;
			}

			/**
			 * Sets the number of mip levels accessible to this view.
			 * @param levelCount Number of mip levels
			 */
			public SubResourceBuilder<T> levelCount(int levelCount) {
				// TODO - validate
				this.levelCount = oneOrMore(levelCount);
				return this;
			}

			/**
			 * Sets the first array layer accessible to this view.
			 * @param baseArrayLayer Base array layer
			 * @throws IllegalArgumentException if the array layer exceeds that of the parent image
			 */
			public SubResourceBuilder<T> baseArrayLayer(int baseArrayLayer) {
				if(baseArrayLayer > layers) {
					throw new IllegalArgumentException(String.format("Sub-resource base array layer %d cannot exceed number of image layers %d", baseArrayLayer, layers));
				}
				this.baseArrayLayer = zeroOrMore(baseArrayLayer);
				return this;
			}

			/**
			 * Sets the number of array layers accessible to this view.
			 * @param layerCount Number of array layers
			 */
			public SubResourceBuilder<T> layerCount(int layerCount) {
				// TODO - validate
				this.layerCount = oneOrMore(layerCount);
				return this;
			}

			/**
			 * Populates an image sub-resource range descriptor
			 * @param range Range descriptor
			 */
			public void populate(VkImageSubresourceRange range) {
				range.aspectMask = mask();
				range.baseMipLevel = mipLevel;
				range.levelCount = levelCount;
				range.baseArrayLayer = baseArrayLayer;
				range.layerCount = layerCount;
			}

			/**
			 * Populates an image sub-resource layers descriptor
			 * @return Layers descriptor
			 */
			public void populate(VkImageSubresourceLayers layers) {
				// TODO - colour => not depth or stencil
				layers.aspectMask = mask();
				layers.mipLevel = mipLevel;
				layers.baseArrayLayer = baseArrayLayer;
				layers.layerCount = layerCount;
			}

			/**
			 * Initialises the aspect mask to the parent image if not explicitly configured by the client.
			 */
			private int mask() {
				if(aspectMask.isEmpty()) {
					return IntegerEnumeration.mask(aspects);
				}
				else {
					return IntegerEnumeration.mask(aspectMask);
				}
			}

			/**
			 * Constructs this image sub-resource.
			 * @return Parent builder
			 */
			public T build() {
				return parent;
			}
		}
	}

	/**
	 * Default implementation.
	 */
	class DefaultImage extends AbstractVulkanObject implements Image {
		private final Descriptor descriptor;
		private final DeviceMemory mem;

		/**
		 * Constructor.
		 * @param handle		Handle
		 * @param dev			Logical device
		 * @param descriptor	Image descriptor
		 * @param mem			Device memory
		 */
		protected DefaultImage(Pointer handle, LogicalDevice dev, Descriptor descriptor, DeviceMemory mem) {
			super(handle, dev);
			this.descriptor = notNull(descriptor);
			this.mem = notNull(mem);
		}

		@Override
		public Descriptor descriptor() {
			return descriptor;
		}

		@Override
		protected Destructor destructor(VulkanLibrary lib) {
			return lib::vkDestroyImage;
		}

		@Override
		protected void release() {
			if(!mem.isDestroyed()) {
				mem.destroy();
			}
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("descriptor", descriptor)
					.append("mem", mem)
					.build();
		}
	}

	/**
	 * Builder for a {@link DefaultImage}.
	 */
	class Builder {
		private Descriptor descriptor;
		private MemoryProperties<VkImageUsage> props;
		private VkSampleCountFlag samples;
		private VkImageTiling tiling = VkImageTiling.OPTIMAL;
		private VkImageLayout layout = VkImageLayout.UNDEFINED;

		public Builder() {
			samples(1);
		}

		/**
		 * Sets the descriptor for this image.
		 * @param descriptor Image descriptor
		 */
		public Builder descriptor(Descriptor descriptor) {
			this.descriptor = notNull(descriptor);
			return this;
		}

		/**
		 * Sets the memory properties for this image.
		 * @param props Memory properties
		 */
		public Builder properties(MemoryProperties<VkImageUsage> props) {
			this.props = notNull(props);
			return this;
		}

		/**
		 * Sets the number of samples.
		 * @param samples Samples-per-texel (default is {@code 1})
		 */
		public Builder samples(int samples) {
			this.samples = IntegerEnumeration.map(VkSampleCountFlag.class, samples);
			return this;
		}

		/**
		 * Sets the image tiling arrangement.
		 * @param tiling Tiling arrangement (default is {@link VkImageTiling#TILING_OPTIMAL})
		 */
		public Builder tiling(VkImageTiling tiling) {
			this.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Sets the initial image layout.
		 * @param lLayout Initial layout (default is undefined)
		 */
		public Builder initialLayout(VkImageLayout layout) {
			this.layout = notNull(layout);
			// TODO - undefined or pre-init only
			return this;
		}

		/**
		 * Constructs this image.
		 * @param dev Logical device
		 * @return New image
		 * @throws VulkanException if the image cannot be created
		 * @throws IllegalArgumentException if the number of array layers is not one for a {@link VkImageType#TYPE_3D} image
		 * @throws VulkanException if the image cannot be created
		 */
		public DefaultImage build(LogicalDevice dev) {
			// Validate
			if(descriptor == null) throw new IllegalArgumentException("No image descriptor specified");
			if(props == null) throw new IllegalArgumentException("No memory properties specified");

			// Populate image structure
			final VkImageCreateInfo info = new VkImageCreateInfo();
			info.imageType = descriptor.type;
			info.format = descriptor.format;
			info.mipLevels = descriptor.levels;
			info.arrayLayers = descriptor.layers;
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = IntegerEnumeration.mask(props.usage());
			info.sharingMode = props.mode();
			descriptor.extents.populate(info.extent);
			// TODO
			//queueFamilyIndexCount;
			// pQueueFamilyIndices;

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateImage(dev.handle(), info, null, handle));

			// Retrieve image memory requirements
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev.handle(), handle.getValue(), reqs);

			// Allocate image memory
			final DeviceMemory mem = dev.allocate(reqs, props);

			// Bind memory to image
			check(lib.vkBindImageMemory(dev.handle(), handle.getValue(), mem.handle(), 0));

			// Create image
			return new DefaultImage(handle.getValue(), dev, descriptor, mem);
		}
	}
}
