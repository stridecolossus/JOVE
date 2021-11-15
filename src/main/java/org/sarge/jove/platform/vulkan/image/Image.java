package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.MathsUtil;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image</i> is a texture or data image stored on the hardware.
 * @author Sarge
 */
public interface Image extends NativeObject {
	/**
	 * Number of array layers for a cube-map image.
	 */
	int CUBEMAP_ARRAY_LAYERS = 6;

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
			final int value = VkFormatFeature.DEPTH_STENCIL_ATTACHMENT.value();
			return MathsUtil.isMask(value, props.optimalTilingFeatures);
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
		private ImageDescriptor descriptor;
		private MemoryProperties<VkImageUsage> props;
		private final Set<VkImageCreateFlag> flags = new HashSet<>();
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
		 * Adds an image creation flag.
		 * @param flag Image creation flag
		 */
		public Builder flag(VkImageCreateFlag flag) {
			flags.add(notNull(flag));
			return this;
		}

		/**
		 * Sets this image as a cube-map.
		 * @see VkImageCreateFlag#CUBE_COMPATIBLE
		 */
		public Builder cubemap() {
			return flag(VkImageCreateFlag.CUBE_COMPATIBLE);
		}

		/**
		 * Sets the number of samples (default is {@code 1}).
		 * @param samples Samples-per-texel
		 */
		public Builder samples(int samples) {
			this.samples = IntegerEnumeration.mapping(VkSampleCountFlag.class).map(samples);
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
			info.flags = IntegerEnumeration.mask(flags);
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

	/**
	 * Image API.
	 */
	interface Library {
		/**
		 * Creates an image.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pImage			Returned image
		 * @return Result code
		 */
		int vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);

		/**
		 * Destroys an image.
		 * @param device			Logical device
		 * @param image				Image
		 * @param pAllocator		Allocator
		 */
		void vkDestroyImage(DeviceContext device, Image image, Pointer pAllocator);

		/**
		 * Retrieves the memory requirements for the given image.
		 * @param device				Logical device
		 * @param image					Image
		 * @param pMemoryRequirements	Returned memory requirements
		 */
		void vkGetImageMemoryRequirements(LogicalDevice device, Pointer image, VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds image memory.
		 * @param device			Logical device
		 * @param image				Image
		 * @param memory			Image memory
		 * @param memoryOffset		Offset
		 * @return Result code
		 */
		int vkBindImageMemory(LogicalDevice device, Pointer image, DeviceMemory memory, long memoryOffset);

		/**
		 * Copies a buffer to an image.
		 * @param commandBuffer		Command
		 * @param srcBuffer			Buffer
		 * @param dstImage			Image
		 * @param dstImageLayout	Image layout
		 * @param regionCount		Number of regions
		 * @param pRegions			Regions
		 */
		void vkCmdCopyBufferToImage(Buffer commandBuffer, VulkanBuffer srcBuffer, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkBufferImageCopy[] pRegions);

		/**
		 * Copies an image to a buffer.
		 * @param commandBuffer		Command
		 * @param srcImage			Image
		 * @param srcImageLayout	Image layout
		 * @param dstBuffer			Buffer
		 * @param regionCount		Number of regions
		 * @param pRegions			Regions
		 */
		void vkCmdCopyImageToBuffer(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, VulkanBuffer dstBuffer, int regionCount, VkBufferImageCopy[] pRegions);
	}
}
