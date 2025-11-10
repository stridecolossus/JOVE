package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

/**
 * A <i>shader</i> is a Vulkan shader module used to implement a programmable pipeline stage.
 * @author Sarge
 */
public class Shader extends VulkanObject {
	/**
	 * Creates a shader module.
	 * @param device	Logical device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(LogicalDevice device, byte[] code) {
		// Create descriptor
		final var info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = code;

		// Allocate shader
		final Library library = device.library();
		final Pointer pointer = new Pointer();
		library.vkCreateShaderModule(device, info, null, pointer);

		// Create shader
		return new Shader(pointer.get(), device);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module
	 * @param device		Logical device
	 */
	Shader(Handle handle, LogicalDevice device) {
		super(handle, device);
	}

	@Override
	protected Destructor<Shader> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyShaderModule;
	}

	/**
	 * Shader module API.
	 */
	interface Library {
		/**
		 * Creates a shader.
		 * @param device			Logical device
		 * @param info				Shader descriptor
		 * @param pAllocator		Allocator
		 * @param shader			Returned shader module handle
		 * @return Result
		 */
		VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader module
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(LogicalDevice device, Shader shader, Handle pAllocator);
	}
}
