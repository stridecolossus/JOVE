package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkPresentInfoKHR;
import org.sarge.jove.platform.vulkan.VkSwapchainCreateInfoKHR;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.render.Swapchain;

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
	int vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);

	/**
	 * Destroys a swap-chain.
	 * @param device			Logical device
	 * @param swapchain			Swap-chain
	 * @param pAllocator		Allocator
	 */
	void vkDestroySwapchainKHR(DeviceContext device, Swapchain swapchain, Pointer pAllocator);

	/**
	 * Retrieves swap-chain image handles.
	 * @param device					Logical device
	 * @param swapchain					Swap-chain handle
	 * @param pSwapchainImageCount		Number of images
	 * @param pSwapchainImages			Image handles
	 * @return Result code
	 */
	int vkGetSwapchainImagesKHR(LogicalDevice device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

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
	int vkAcquireNextImageKHR(DeviceContext device, Swapchain swapchain, long timeout, Semaphore semaphore, Fence fence, IntByReference pImageIndex);

	/**
	 * Presents to the swapchain.
	 * @param queue					Presentation queue
	 * @param pPresentInfo			Pointer to descriptor
	 * @return Result code
	 */
	int vkQueuePresentKHR(Queue queue, VkPresentInfoKHR pPresentInfo);
}
