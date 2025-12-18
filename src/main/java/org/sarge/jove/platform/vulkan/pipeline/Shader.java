package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

/**
 * A <i>shader</i> module implements a programmable pipeline stage.
 * @author Sarge
 */
public class Shader extends VulkanObject {
	/**
	 * Creates a shader module.
	 * @param device	Logical device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(DeviceContext device, byte[] code) {
		// Create descriptor
		final var info = new VkShaderModuleCreateInfo();
		info.sType = VkStructureType.SHADER_MODULE_CREATE_INFO;
		info.codeSize = code.length;
		info.pCode = code;

		// Allocate shader
		final Library library = device.library();
		final Pointer pointer = new Pointer();
		library.vkCreateShaderModule(device, info, null, pointer);

		// Create shader
		return new Shader(pointer.handle(), device);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module
	 * @param device		Logical device
	 */
	Shader(Handle handle, DeviceContext device) {
		super(handle, device);
	}

	@Override
	protected Destructor<Shader> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyShaderModule;
	}

	/**
	 * Loader for a shader.
	 */
	public static class ShaderLoader {
		private final LogicalDevice device;

		/**
		 * Constructor.
		 * @param device Logical device
		 */
		public ShaderLoader(LogicalDevice device) {
			this.device = requireNonNull(device);
		}

		/**
		 * Loads a shader from the given file.
		 * @param path File path
		 * @return Shader
		 * @throws IOException if the shader cannot be loaded
		 */
		public Shader load(Path path) throws IOException {
			final byte[] code = Files.readAllBytes(path);
			return Shader.create(device, code);
		}
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
		VkResult vkCreateShaderModule(DeviceContext device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader module
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(DeviceContext device, Shader shader, Handle pAllocator);
	}
}
