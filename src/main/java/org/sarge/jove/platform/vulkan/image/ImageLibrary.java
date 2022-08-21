package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface ImageLibrary extends View.Library, Sampler.Library {
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
	 * Copies an image.
	 * @param commandBuffer		Command buffer
	 * @param srcImage			Source image
	 * @param srcImageLayout	Source layout
	 * @param dstImage			Destination image
	 * @param dstImageLayou		Destination layout
	 * @param regionCount		Number of regions
	 * @param pRegions			Regions
	 */
	void vkCmdCopyImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayou, int regionCount, VkImageCopy[] pRegions);

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
