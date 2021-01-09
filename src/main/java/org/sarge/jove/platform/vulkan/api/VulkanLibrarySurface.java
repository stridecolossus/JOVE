package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;

import com.sun.jna.ptr.IntByReference;

/**
 * Vulkan surface API.
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
	int vkGetPhysicalDeviceSurfaceSupportKHR(Handle device, int queueFamilyIndex, Handle surface, IntByReference supported);

	/**
	 * Retrieves the capabilities of a surface.
	 * @param device			Physical device
	 * @param surface			Surface handle
	 * @param caps				Returned capabilities
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(Handle device, Handle surface, VkSurfaceCapabilitiesKHR caps);

	/**
	 * Queries the supported surface formats.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param formats			Supported formats
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceFormatsKHR(Handle device, Handle surface, IntByReference count, VkSurfaceFormatKHR formats);

	/**
	 * Queries the supported presentation modes.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param modes				Supported presentation modes
	 * @return Result
	 * @see VkPresentModeKHR
	 */
	int vkGetPhysicalDeviceSurfacePresentModesKHR(Handle device, Handle surface, IntByReference count, int[] modes);

	/**
	 * Destroys a surface.
	 * @param instance			Vulkan instance
	 * @param surface			Surface
	 * @param allocator			Allocator
	 */
	void vkDestroySurfaceKHR(Handle instance, Handle surface, Handle allocator);
}
