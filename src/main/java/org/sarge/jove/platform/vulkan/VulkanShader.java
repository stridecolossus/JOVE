package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.sarge.jove.util.BufferFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan shader implementation.
 * @author Sarge
 */
public class VulkanShader extends VulkanHandle {
	/**
	 * Creates a shader.
	 * @param dev		Device
	 * @param code		Shader code
	 * @return Shader
	 */
	public static VulkanShader create(LogicalDevice dev, byte[] code) {
		// Buffer shader code
		final ByteBuffer buffer = BufferFactory.byteBuffer(code.length);
		buffer.put(code);
		buffer.flip();

		// Create shader descriptor
		final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = buffer;

		// Create shader
		final Vulkan vulkan = dev.parent().vulkan();
		final VulkanLibraryShader lib = vulkan.library();
		final PointerByReference shader = vulkan.factory().reference();
		check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

		// Create shader wrapper
		// TODO - tracking
		final Pointer handle = shader.getValue();
		final Destructor destructor = () -> lib.vkDestroyShaderModule(dev.handle(), handle, null);
		return new VulkanShader(new VulkanHandle(handle, destructor));
	}

	/**
	 * Creates a shader from the given input stream.
	 * @param dev		Device
	 * @param code		Shader code
	 * @return Shader
	 * @throws IOException if the shader cannot be loaded
	 */
	public static VulkanShader create(LogicalDevice dev, InputStream in) throws IOException {
		return create(dev, in.readAllBytes());
	}

	/**
	 * Constructor.
	 * @param handle Shader handle
	 */
	VulkanShader(VulkanHandle handle) {
		super(handle);
	}

	// TODO
	// - select
	// - parameters
	// - peer
	// - upload data
}
