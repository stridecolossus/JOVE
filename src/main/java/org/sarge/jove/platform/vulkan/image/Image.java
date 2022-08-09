package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A Vulkan <i>image</i> is a texture or data image stored on the hardware.
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
	 * Default implementation for an image managed by the application.
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
		protected DefaultImage(Pointer handle, DeviceContext dev, ImageDescriptor descriptor, DeviceMemory mem) {
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
		private MemoryProperties<VkImageUsageFlag> props;
		private final Set<VkImageCreateFlag> flags = new HashSet<>();
		private VkSampleCount samples = VkSampleCount.COUNT_1;
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
		public Builder properties(MemoryProperties<VkImageUsageFlag> props) {
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
		 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
		 */
		public Builder samples(int samples) {
			this.samples = IntegerEnumeration.reverse(VkSampleCount.class).map(samples);
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
			final var info = new VkImageCreateInfo();
			info.flags = IntegerEnumeration.reduce(flags);
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = IntegerEnumeration.reduce(props.usage());
			info.sharingMode = props.mode();
			// TODO - queueFamilyIndexCount, pQueueFamilyIndices

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
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
		 * @return Result
		 */
		int vkCreateImage(DeviceContext device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);

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
		void vkGetImageMemoryRequirements(DeviceContext device, Pointer image, VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds image memory.
		 * @param device			Logical device
		 * @param image				Image
		 * @param memory			Image memory
		 * @param memoryOffset		Offset
		 * @return Result
		 */
		int vkBindImageMemory(DeviceContext device, Pointer image, DeviceMemory memory, long memoryOffset);

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

		/**
		 * Performs an image blit operation.
		 * @param commandBuffer		Command
		 * @param srcImage			Source image
		 * @param srcImageLayout	Source image layout
		 * @param dstImage			Destination image
		 * @param dstImageLayout	Destination image layout
		 * @param regionCount		Number of blit regions
		 * @param pRegions			Copy regions
		 * @param filter			Filtering option
		 */
		void vkCmdBlitImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageBlit[] pRegions, VkFilter filter);
	}
}
