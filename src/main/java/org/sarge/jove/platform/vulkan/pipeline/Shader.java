package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>shader</i> is a Vulkan shader module used to implement a programmable pipeline stage.
 * @author Sarge
 */
public final class Shader extends VulkanObject {
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
		info.pCode = requireNonNull(code);

		// Allocate shader
		final VulkanLibrary vulkan = device.vulkan();
		final Pointer ref = new Pointer();
		vulkan.vkCreateShaderModule(device, info, null, ref);

		// Create shader
		return new Shader(ref.get(), device);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Logical device
	 */
	private Shader(Handle handle, LogicalDevice dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Shader> destructor(VulkanLibrary lib) {
		return lib::vkDestroyShaderModule;
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
