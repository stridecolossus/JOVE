package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPresentInfoKHR;
import org.sarge.jove.platform.vulkan.VkSwapchainCreateInfoKHR;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Swap-chain API.
 */
interface VulkanLibrarySwapchain {
	/**
	 * Creates a swap-chain for the given device.
	 * @param device			Logical device
	 * @param pCreateInfo		Swap-chain descriptor
	 * @param pAllocator		Allocator
	 * @param pSwapchain		Returned swap-chain handle
	 * @return Result code
	 */
	int vkCreateSwapchainKHR(Handle device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, PointerByReference pSwapchain);

	/**
	 * Destroys a swap-chain.
	 * @param device			Logical device
	 * @param swapchain			Swap-chain
	 * @param pAllocator		Allocator
	 */
	void vkDestroySwapchainKHR(Handle device, Handle swapchain, Handle pAllocator);

	/**
	 * Retrieves swap-chain image handles.
	 * @param device					Logical device
	 * @param swapchain					Swap-chain handle
	 * @param pSwapchainImageCount		Number of images
	 * @param pSwapchainImages			Image handles
	 * @return Result code
	 */
	int vkGetSwapchainImagesKHR(Handle device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

	/**
	 * Acquires the next image in the swap-chain.
	 * @param device				Logical device
	 * @param swapchain				Swap-chain
	 * @param timeout				Timeout (ns) or {@link Long#MAX_VALUE} to disable
	 * @param semaphore				Optional semaphore
	 * @param fence					Optional fence
	 * @param pImageIndex			Returned image index
	 * @return Result code
	 */
	int vkAcquireNextImageKHR(Handle device, Handle swapchain, long timeout, Handle semaphore, Handle fence, IntByReference pImageIndex);

	/**
	 * Presents to the swapchain.
	 * @param queue					Presentation queue
	 * @param pPresentInfo			Pointer to descriptor
	 * @return Result code
	 */
	int vkQueuePresentKHR(Handle queue, VkPresentInfoKHR pPresentInfo);
}
