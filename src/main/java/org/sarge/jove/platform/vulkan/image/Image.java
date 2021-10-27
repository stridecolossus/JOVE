package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.MathsUtil;

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
	ImageDescriptor descriptor();

	/**
	 * @return Device context for this image
	 */
	DeviceContext device();

	/**
	 * Helper - Selects an image format for a depth-stencil with optimal tiling.
	 * @param dev Physical device
	 * @return Selected depth buffer format
	 * @throws RuntimeException if there is no supported format
	 */
	static VkFormat depth(PhysicalDevice dev) {
		// Filter for depth-stencil formats with optimal tiling
		final Predicate<VkFormat> predicate = format -> {
			final VkFormatProperties props = dev.properties(format);
			return MathsUtil.isMask(props.optimalTilingFeatures, VkFormatFeature.DEPTH_STENCIL_ATTACHMENT.value());
		};

		// Select from candidate formats
		return Stream
				.of(VkFormat.D32_SFLOAT, VkFormat.D32_SFLOAT_S8_UINT, VkFormat.D24_UNORM_S8_UINT)
				.filter(predicate)
				.findAny()
				.orElseThrow(() -> new RuntimeException("No supported depth buffer format"));
	}

	/**
	 * Default implementation managed by the application.
	 */
	class DefaultImage extends AbstractVulkanObject implements Image {
		private final ImageDescriptor descriptor;
		private final DeviceMemory mem;

		/**
		 * Constructor.
		 * @param handle		Handle
		 * @param dev			Logical device
		 * @param descriptor	Image descriptor
		 * @param mem			Device memory
		 */
		protected DefaultImage(Pointer handle, LogicalDevice dev, ImageDescriptor descriptor, DeviceMemory mem) {
			super(handle, dev);
			this.descriptor = notNull(descriptor);
			this.mem = notNull(mem);
		}

		@Override
		public ImageDescriptor descriptor() {
			return descriptor;
		}

		DeviceMemory memory() {
			return mem;
		}

		@Override
		protected Destructor<DefaultImage> destructor(VulkanLibrary lib) {
			return lib::vkDestroyImage;
		}

		@Override
		protected void release() {
			if(!mem.isDestroyed()) {
				mem.close();
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
		private ImageDescriptor descriptor;
		private MemoryProperties<VkImageUsage> props;
		private VkSampleCountFlag samples = VkSampleCountFlag.COUNT_1;
		private VkImageTiling tiling = VkImageTiling.OPTIMAL;
		private VkImageLayout layout = VkImageLayout.UNDEFINED;

		/**
		 * Sets the descriptor for this image.
		 * @param descriptor Image descriptor
		 */
		public Builder descriptor(ImageDescriptor descriptor) {
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
		 * Sets the number of samples (default is {@code 1}).
		 * @param samples Samples-per-texel
		 */
		public Builder samples(int samples) {
			this.samples = IntegerEnumeration.map(VkSampleCountFlag.class, samples);
			return this;
		}

		/**
		 * Sets the image tiling arrangement (default is {@link VkImageTiling#TILING_OPTIMAL}).
		 * @param tiling Tiling arrangement
		 */
		public Builder tiling(VkImageTiling tiling) {
			this.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Sets the initial image layout (default is {@link VkImageLayout#UNDEFINED}).
		 * @param layout Initial layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			if((layout != VkImageLayout.UNDEFINED) && (layout != VkImageLayout.PREINITIALIZED)) {
				throw new IllegalArgumentException("Invalid initial layout: " + layout);
			}
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Constructs this image.
		 * @param dev 			Logical device
		 * @param allocator		Memory allocator
		 * @return New image
		 * @throws IllegalArgumentException if the number of array layers is not one for a {@link VkImageType#TYPE_3D} image
		 * @throws VulkanException if the image cannot be created
		 */
		public DefaultImage build(LogicalDevice dev, AllocationService allocator) {
			// Validate
			if(descriptor == null) throw new IllegalArgumentException("No image descriptor specified");
			if(props == null) throw new IllegalArgumentException("No memory properties specified");

			// Populate image structure
			final VkImageCreateInfo info = new VkImageCreateInfo();
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent3D();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = IntegerEnumeration.mask(props.usage());
			info.sharingMode = props.mode();
			// TODO
			//queueFamilyIndexCount;
			// pQueueFamilyIndices;

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateImage(dev, info, null, handle));

			// Retrieve image memory requirements
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev, handle.getValue(), reqs);

			// Allocate image memory
			final DeviceMemory mem = allocator.allocate(reqs, props);

			// Bind memory to image
			check(lib.vkBindImageMemory(dev, handle.getValue(), mem, 0));

			// Create image
			return new DefaultImage(handle.getValue(), dev, descriptor, mem);
		}
	}
}
