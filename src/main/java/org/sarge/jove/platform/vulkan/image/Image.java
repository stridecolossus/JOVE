package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;

/**
 * A Vulkan <i>image</i> is a texture or data image stored on the hardware.
 * @author Sarge
 */
public interface Image extends NativeObject, TransientObject {
	/**
	 * Number of array layers for a cube-map image.
	 */
	int CUBEMAP_ARRAY_LAYERS = 6;

	/**
	 * @return Descriptor for this image
	 */
	Descriptor descriptor();

	/**
	 * An <i>image descriptor</i> specifies the properties of this image.
	 * Note that an image descriptor is also a {@link Subresource} with default (i.e. zero) values for the {@link #mipLevel()} and {@link #baseArrayLayer()} properties.
	 */
	record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlags> aspects, int levelCount, int layerCount) implements Subresource {
		/**
		 * Constructor.
		 * @param type			Image type
		 * @param format		Format
		 * @param extents		Image extents
		 * @param aspects		Image aspect(s)
		 * @param levelCount	Number of mip levels
		 * @param layerCount	Number of array layers
		 * @throws IllegalArgumentException if {@link #extents} is invalid for the given image {@link #type}
		 */
		public Descriptor {
			requireNonNull(type);
			requireNonNull(format);
			requireNonNull(extents);
			aspects = Set.copyOf(aspects);
			requireOneOrMore(levelCount);
			requireOneOrMore(layerCount);

			if(!extents.isValid(type)) {
				throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
			}

			if((type == VkImageType.TYPE_3D) && (layerCount != 1)) {
				throw new IllegalArgumentException("Array layers must be one for a 3D image");
			}
		}

		@Override
		public int mipLevel() {
			return 0;
		}

		@Override
		public int baseArrayLayer() {
			return 0;
		}

		/**
		 * Builder for an image descriptor.
		 */
		public static class Builder {
			private VkImageType type = VkImageType.TYPE_2D;
			private VkFormat format;
			private Extents extents;
			private final Set<VkImageAspectFlags> aspects = new HashSet<>();
			private int levels = 1;
			private int layers = 1;

			/**
			 * Sets the image type (default is a 2D image).
			 * @param type Image type
			 */
			public Builder type(VkImageType type) {
				this.type = type;
				return this;
			}

			/**
			 * Sets the image format.
			 * @param format Image format
			 */
			public Builder format(VkFormat format) {
				this.format = format;
				return this;
			}

			/**
			 * Sets the image extents.
			 * @param size Image dimensions
			 */
			public Builder extents(Extents extents) {
				this.extents = extents;
				return this;
			}

			/**
			 * Convenience setter for the extents of a 2D image.
			 * @param size Image dimensions
			 */
			public Builder extents(Dimensions size) {
				return extents(new Extents(size));
			}

			/**
			 * Adds an image aspect.
			 * @param aspect Image aspect
			 */
			public Builder aspect(VkImageAspectFlags aspect) {
				aspects.add(aspect);
				return this;
			}

			/**
			 * Sets the number of mip levels (default is one).
			 * @param levels Number of mip levels
			 */
			public Builder mipLevels(int levels) {
				this.levels = levels;
				return this;
			}

			/**
			 * Sets the number of array levels (default is one).
			 * @param levels Number of array levels
			 */
			public Builder arrayLayers(int layers) {
				this.layers = layers;
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
	}

	/**
	 * Vulkan image library.
	 */
	interface Library {
		/**
		 * Creates an image.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pImage			Returned image handle
		 */
		VkResult vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Handle pAllocator, Pointer pImage);

		/**
		 * Destroys an image.
		 * @param device			Logical device
		 * @param image				Image
		 * @param pAllocator		Allocator
		 */
		void vkDestroyImage(LogicalDevice device, Image image, Handle pAllocator);

		/**
		 * Retrieves the memory requirements for the given image.
		 * @param device				Logical device
		 * @param image					Image
		 * @param pMemoryRequirements	Returned memory requirements
		 */
		void vkGetImageMemoryRequirements(LogicalDevice device, Handle image, @Updated VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds image memory.
		 * @param device			Logical device
		 * @param image				Image
		 * @param memory			Image memory
		 * @param memoryOffset		Offset
		 */
		VkResult vkBindImageMemory(LogicalDevice device, Handle image, DeviceMemory memory, long memoryOffset);

		/**
		 * Copies an image.
		 * @param commandBuffer		Command buffer
		 * @param srcImage			Source image
		 * @param srcImageLayout	Source layout
		 * @param dstImage			Destination image
		 * @param dstImageLayout	Destination layout
		 * @param regionCount		Number of regions
		 * @param pRegions			Regions
		 */
		void vkCmdCopyImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageCopy[] pRegions);

		/**
		 * Copies a buffer to an image.
		 * @param commandBuffer		Command buffer
		 * @param srcBuffer			Buffer
		 * @param dstImage			Image
		 * @param dstImageLayout	Image layout
		 * @param regionCount		Number of regions
		 * @param pRegions			Regions
		 */
		void vkCmdCopyBufferToImage(Buffer commandBuffer, VulkanBuffer srcBuffer, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkBufferImageCopy[] pRegions);

		/**
		 * Copies an image to a buffer.
		 * @param commandBuffer		Command buffer
		 * @param srcImage			Image
		 * @param srcImageLayout	Image layout
		 * @param dstBuffer			Buffer
		 * @param regionCount		Number of regions
		 * @param pRegions			Regions
		 */
		void vkCmdCopyImageToBuffer(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, VulkanBuffer dstBuffer, int regionCount, VkBufferImageCopy[] pRegions);

		/**
		 * Performs an image blit operation.
		 * @param commandBuffer		Command buffer
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
