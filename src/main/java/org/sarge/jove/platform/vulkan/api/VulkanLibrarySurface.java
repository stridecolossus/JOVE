package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.core.Surface;

import com.sun.jna.Pointer;
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
	int vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Surface surface, IntByReference supported);

	/**
	 * Retrieves the capabilities of a surface.
	 * @param device			Physical device
	 * @param surface			Surface handle
	 * @param caps				Returned capabilities
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, Surface surface, VkSurfaceCapabilitiesKHR caps);

	/**
	 * Queries the supported surface formats.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param formats			Supported formats
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, Surface surface, IntByReference count, VkSurfaceFormatKHR formats);

	/**
	 * Queries the supported presentation modes.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param modes				Supported presentation modes
	 * @return Result
	 * @see VkPresentModeKHR
	 */
	int vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, Surface surface, IntByReference count, int[] modes);

	/**
	 * Destroys a surface.
	 * @param instance			Vulkan instance
	 * @param surface			Surface
	 * @param allocator			Allocator
	 */
	void vkDestroySurfaceKHR(Instance instance, Surface surface, Pointer allocator);
}
