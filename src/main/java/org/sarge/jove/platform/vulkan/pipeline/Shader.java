package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.io.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.io.*;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>shader</i> is a Vulkan shader module used to implement a programmable pipeline stage.
 * @author Sarge
 */
public final class Shader extends VulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Logical device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(DeviceContext dev, byte[] code) {
		// Create descriptor
		final var info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = BufferHelper.buffer(code);

		// Allocate shader
		final Vulkan vulkan = dev.vulkan();
		final PointerReference ref = vulkan.factory().pointer();
		vulkan.library().vkCreateShaderModule(dev, info, null, ref);

		// Create shader
		return new Shader(ref.handle(), dev);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Logical device
	 */
	private Shader(Handle handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Shader> destructor(VulkanLibrary lib) {
		return lib::vkDestroyShaderModule;
	}

	/**
	 * Loader for a shader.
	 */
	public static class Loader implements ResourceLoader<InputStream, Shader> {
		private final DeviceContext dev;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Loader(DeviceContext dev) {
			this.dev = requireNonNull(dev);
		}

		@Override
		public InputStream map(InputStream in) throws IOException {
			return in;
		}

		@Override
		public Shader load(InputStream in) throws IOException {
			final byte[] code = in.readAllBytes();
			return create(dev, code);
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
		 * @param shader			Returned shader module
		 * @return Result
		 */
		int vkCreateShaderModule(DeviceContext device, VkShaderModuleCreateInfo info, Handle pAllocator, PointerReference shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader module
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(DeviceContext device, Shader shader, Handle pAllocator);
	}
}
