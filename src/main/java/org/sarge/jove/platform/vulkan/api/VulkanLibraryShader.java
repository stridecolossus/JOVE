package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.Shader;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan shader module API.
 */
interface VulkanLibraryShader {
	/**
	 * Create a shader.
	 * @param device			Logical device
	 * @param info				Shader descriptor
	 * @param pAllocator		Allocator
	 * @param shader			Returned shader handle
	 * @return Result code
	 */
	int vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Pointer pAllocator, PointerByReference shader);

	/**
	 * Destroys a shader.
	 * @param device			Logical device
	 * @param shader			Shader
	 * @param pAllocator		Allocator
	 */
	void vkDestroyShaderModule(DeviceContext device, Shader shader, Pointer pAllocator);
}
