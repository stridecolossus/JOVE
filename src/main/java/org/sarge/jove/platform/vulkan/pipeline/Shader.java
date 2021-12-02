package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;

import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a Vulkan shader module.
 * @author Sarge
 */
public class Shader extends AbstractVulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Parent device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(DeviceContext dev, byte[] code) {
		// Create descriptor
		final var info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = BufferWrapper.buffer(code);

		// Allocate shader
		final VulkanLibrary lib = dev.library();
		final PointerByReference shader = dev.factory().pointer();
		check(lib.vkCreateShaderModule(dev, info, null, shader));

		// Create shader
		return new Shader(shader.getValue(), dev);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Device
	 */
	private Shader(Pointer handle, DeviceContext dev) {
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
			this.dev = notNull(dev);
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
		 * Create a shader.
		 * @param device			Logical device
		 * @param info				Shader descriptor
		 * @param pAllocator		Allocator
		 * @param shader			Returned shader handle
		 * @return Result code
		 */
		int vkCreateShaderModule(DeviceContext device, VkShaderModuleCreateInfo info, Pointer pAllocator, PointerByReference shader);

		/**
		 * Destroys a shader.
		 * @param device			Logical device
		 * @param shader			Shader
		 * @param pAllocator		Allocator
		 */
		void vkDestroyShaderModule(DeviceContext device, Shader shader, Pointer pAllocator);
	}
}
