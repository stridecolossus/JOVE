package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan API for instance management.
 */
interface VulkanLibraryInstance {
	/**
	 * Creates a vulkan instance.
	 * @param info			Instance descriptor
	 * @param allocator		Allocator
	 * @param instance		Returned instance
	 * @return Result
	 */
	int vkCreateInstance(VkInstanceCreateInfo info, Pointer allocator, PointerByReference instance);

	/**
	 * Destroys the vulkan instance.
	 * @param instance		Instance handle
	 * @param allocator		Allocator
	 */
	void vkDestroyInstance(Pointer instance, Pointer allocator);

	/**
	 * Enumerates extension properties.
	 * @param filter		Layer name or <tt>null</tt> for all
	 * @param count			Number of extensions
	 * @param extensions	Extensions
	 * @return Result
	 */
	int vkEnumerateInstanceExtensionProperties(String pLayerName, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates validation layer properties.
	 * @param count			Number of layers
	 * @param layers		Layers
	 * @return Result
	 */
	int vkEnumerateInstanceLayerProperties(IntByReference count, VkLayerProperties layers);
}
