package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.BufferFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a reference to a Vulkan shader module.
 * @author Sarge
 */
public class Shader {
	/**
	 * Creates a shader module.
	 * @param dev		Parent device
	 * @param code		Shader code
	 * @return New shader
	 */
	public static Shader create(LogicalDevice dev, byte[] code) {
		// Buffer shader code
		final ByteBuffer buffer = BufferFactory.byteBuffer(code.length);
		buffer.put(code);
		buffer.flip();

		// Create descriptor
		final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = buffer;

		// Allocate shader
		final VulkanLibrary lib = dev.library();
		final PointerByReference shader = lib.factory().pointer();
		check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

		// Create shader
		return new Shader(shader.getValue(), dev);
	}

	private final Handle handle;
	private final LogicalDevice dev;

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Device
	 */
	private Shader(Pointer handle, LogicalDevice dev) {
		this.handle = new Handle(handle);
		this.dev = notNull(dev);
	}

	/**
	 * @return Shader module handle
	 */
	public Handle handle() {
		return handle;
	}

	/**
	 * Destroys this shader module.
	 */
	public void destroy() {
		dev.library().vkDestroyShaderModule(dev.handle(), handle, null);
	}
}
