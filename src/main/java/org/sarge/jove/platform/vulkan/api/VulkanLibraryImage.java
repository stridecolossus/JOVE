package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkImageCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSamplerCreateInfo;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.render.Sampler;

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

	/**
	 * Creates an image view.
	 * @param device			Logical device
	 * @param pCreateInfo		Image view descriptor
	 * @param pAllocator		Allocator
	 * @param pView				Returned image view handle
	 * @return Result code
	 */
	int vkCreateImageView(DeviceContext device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);

	/**
	 * Destroys an image view.
	 * @param device			Logical device
	 * @param imageView			Image view
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImageView(DeviceContext device, View imageView, Pointer pAllocator);

	/**
	 * Creates an image sampler.
	 * @param device			Logical device
	 * @param pCreateInfo		Sampler descriptor
	 * @param pAllocator		Allocator
	 * @param pSampler			Returned sampler handle
	 * @return Result code
	 */
	int vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSampler);

	/**
	 * Destroys a sampler.
	 * @param device			Logical device
	 * @param sampler			Sampler
	 * @param pAllocator		Allocator
	 */
	void vkDestroySampler(DeviceContext device, Sampler sampler, Pointer pAllocator);
}
