package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan surface and swap-chain API.
 * @author Sarge
 */
public interface VulkanLibrarySurface {
	/**
	 * Queries whether a queue family supports presentation for the given surface.
	 * @param device				Physical device handle
	 * @param queueFamilyIndex		Queue family
	 * @param surface				Vulkan surface
	 * @param supported				Returned boolean flag
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceSupportKHR(Pointer device, int queueFamilyIndex, Pointer surface, IntByReference supported);

	/**
	 * Retrieves the capabilities of a surface.
	 * @param device			Physical device
	 * @param surface			Surface handle
	 * @param caps				Returned capabilities
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(Pointer device, Pointer surface, VkSurfaceCapabilitiesKHR caps);

	/**
	 * Queries the supported surface formats.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param formats			Supported formats
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceFormatsKHR(Pointer device, Pointer surface, IntByReference count, VkSurfaceFormatKHR formats);

	/**
	 * Queries the supported presentation modes.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param modes				Supported presentation modes
	 * @return Result
	 * @see VkPresentModeKHR
	 */
	int vkGetPhysicalDeviceSurfacePresentModesKHR(Pointer device, Pointer surface, IntByReference count, int[] modes); // PointerByReference modes);

	/**
	 * Destroys a surface.
	 * @param instance			Vulkan instance
	 * @param surface			Surface
	 * @param allocator			Allocator
	 */
	void vkDestroySurfaceKHR(Pointer instance, Pointer surface, Pointer allocator);

	/**
	 * Creates a swap-chain for the given device.
	 * @param device			Logical device
	 * @param pCreateInfo		Swap-chain descriptor
	 * @param pAllocator		Allocator
	 * @param pSwapchain		Returned swap-chain handle
	 * @return Result code
	 */
	int vkCreateSwapchainKHR(Pointer device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);

	/**
	 * Destroys a swap-chain.
	 * @param device			Logical device
	 * @param swapchain			Swap-chain
	 * @param pAllocator		Allocator
	 */
	void vkDestroySwapchainKHR(Pointer device, Pointer swapchain, Pointer pAllocator);

	/**
	 * Retrieves swap-chain image handles.
	 * @param device					Logical device
	 * @param swapchain					Swap-chain handle
	 * @param pSwapchainImageCount		Number of images
	 * @param pSwapchainImages			Image handles
	 * @return Result code
	 */
	int vkGetSwapchainImagesKHR(Pointer device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

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
	int vkAcquireNextImageKHR(Pointer device, Pointer swapchain, long timeout, Pointer semaphore, Pointer fence, IntByReference pImageIndex);
}
