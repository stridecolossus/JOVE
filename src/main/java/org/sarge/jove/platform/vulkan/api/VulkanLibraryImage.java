package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.NativeObject.Handle;
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
	int vkCreateImage(Handle device, VkImageCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pImage);

	/**
	 * Destroys an image.
	 * @param device			Logical device
	 * @param image				Image
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImage(Handle device, Handle image, Handle pAllocator);

	/**
	 * Retrieves the memory requirements for the given image.
	 * @param device				Logical device
	 * @param image					Image
	 * @param pMemoryRequirements	Returned memory requirements
	 */
	void vkGetImageMemoryRequirements(Handle device, Pointer image, VkMemoryRequirements pMemoryRequirements);

	/**
	 * Binds image memory.
	 * @param device			Logical device
	 * @param image				Image
	 * @param memory			Image memory
	 * @param memoryOffset		Offset
	 * @return Result code
	 */
	int vkBindImageMemory(Handle device, Pointer image, Handle memory, long memoryOffset);

	void vkCmdCopyBufferToImage(Handle commandBuffer, Handle srcBuffer, Handle dstImage, VkImageLayout dstImageLayout, int regionCount, VkBufferImageCopy[] pRegions);
	void vkCmdCopyImageToBuffer(Handle commandBuffer, Handle srcImage, VkImageLayout srcImageLayout, Handle dstBuffer, int regionCount, VkBufferImageCopy[] pRegions);

	/**
	 * Creates an image view.
	 * @param device			Logical device
	 * @param pCreateInfo		Image view descriptor
	 * @param pAllocator		Allocator
	 * @param pView				Returned image view handle
	 * @return Result code
	 */
	int vkCreateImageView(Handle device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pView);

	/**
	 * Destroys an image view.
	 * @param device			Logical device
	 * @param imageView			Image view
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImageView(Handle device, Handle imageView, Handle pAllocator);

	/**
	 * Creates an image sampler.
	 * @param device			Logical device
	 * @param pCreateInfo		Sampler descriptor
	 * @param pAllocator		Allocator
	 * @param pSampler			Returned sampler handle
	 * @return Result code
	 */
	int vkCreateSampler(Handle device, VkSamplerCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pSampler);

	/**
	 * Destroys a sampler.
	 * @param device			Logical device
	 * @param sampler			Sampler
	 * @param pAllocator		Allocator
	 */
	void vkDestroySampler(Handle device, Handle sampler, Handle pAllocator);
}