package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkImageCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSamplerCreateInfo;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Image and views API.
 */
interface VulkanLibraryImage {
	/**
	 * Creates an image.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pImage			Returned image
	 * @return Result code
	 */
	int vkCreateImage(Pointer device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);

	/**
	 * Destroys an image.
	 * @param device			Logical device
	 * @param image				Image
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImage(Pointer device, Pointer image, Pointer pAllocator);

	/**
	 * Retrieves the memory requirements for the given image.
	 * @param device				Logical device
	 * @param image					Image
	 * @param pMemoryRequirements	Returned memory requirements
	 */
	void vkGetImageMemoryRequirements(Pointer device, Pointer image, VkMemoryRequirements pMemoryRequirements);

	/**
	 * Binds image memory.
	 * @param device			Logical device
	 * @param image				Image
	 * @param memory			Image memory
	 * @param memoryOffset		Offset
	 * @return Result code
	 */
	int vkBindImageMemory(Pointer device, Pointer image, Pointer memory, long memoryOffset);

	void vkCmdCopyBufferToImage(Pointer commandBuffer, Pointer srcBuffer, Pointer dstImage, VkImageLayout dstImageLayout, int regionCount, VkBufferImageCopy pRegions);
	void vkCmdCopyImageToBuffer(Pointer commandBuffer, Pointer srcImage, VkImageLayout srcImageLayout, Pointer dstBuffer, int regionCount, VkBufferImageCopy pRegions);

	/**
	 * Creates an image view.
	 * @param device			Logical device
	 * @param pCreateInfo		Image view descriptor
	 * @param pAllocator		Allocator
	 * @param pView				Returned image view handle
	 * @return Result code
	 */
	int vkCreateImageView(Pointer device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);

	/**
	 * Destroys an image view.
	 * @param device			Logical device
	 * @param imageView			Image view
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImageView(Pointer device, Pointer imageView, Pointer pAllocator);

	int vkCreateSampler(Pointer device, VkSamplerCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSampler);
	void vkDestroySampler(Pointer device, Pointer sampler, Pointer pAllocator);
}