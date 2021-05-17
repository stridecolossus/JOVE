package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;

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
	int vkCreateShaderModule(Handle device, VkShaderModuleCreateInfo info, Handle pAllocator, PointerByReference shader);

	/**
	 * Destroys a shader.
	 * @param device			Logical device
	 * @param shader			Shader
	 * @param pAllocator		Allocator
	 */
	void vkDestroyShaderModule(Handle device, Handle shader, Handle pAllocator);
}
