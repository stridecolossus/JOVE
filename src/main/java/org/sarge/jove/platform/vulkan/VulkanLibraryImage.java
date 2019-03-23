package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan image API.
 * @author Sarge
 */
public interface VulkanLibraryImage {
	/**
	 * Creates an image.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pImage			Returned image
	 * @return Result code
	 */
	int vkCreateImage(Pointer device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, Pointer pImage);

	/**
	 * Destroys an image.
	 * @param device			Logical device
	 * @param image				Image
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImage(Pointer device, Pointer image, Pointer pAllocator);

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
}
