package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.io.IOException;
import java.io.InputStream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.Loader;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a reference to a Vulkan shader module.
 * @author Sarge
 */
public class Shader extends AbstractVulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Parent device
	 * @param code		Shader SPIV code
	 * @return New shader
	 */
	public static Shader create(LogicalDevice dev, byte[] code) {
		// Create descriptor
		final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = Bufferable.allocate(code);

		// Allocate shader
		final VulkanLibrary lib = dev.library();
		final PointerByReference shader = lib.factory().pointer();
		check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

		// Create shader
		return new Shader(shader.getValue(), dev);
	}

	/**
	 * Creates a shader loader.
	 * @param dev Logical device
	 * @return Shader loader
	 */
	public static Loader<InputStream, Shader> loader(LogicalDevice dev) {
		return in -> {
			try {
				final byte[] code = in.readAllBytes();
				return create(dev, code);
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Device
	 */
	private Shader(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyShaderModule);
	}
}
