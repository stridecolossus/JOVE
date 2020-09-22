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
 * TODO - opaque handle? i.e. no functionality
 */
public class VulkanShader extends LogicalDeviceHandle {
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

		// Allocate shader
		final Vulkan vulkan = dev.vulkan();
		final VulkanLibraryShader lib = vulkan.api();
		final PointerByReference shader = vulkan.factory().reference();
		check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

		// Create shader
		// TODO - tracking
		return new VulkanShader(shader.getValue(), dev);
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
	VulkanShader(Pointer handle, LogicalDevice dev) {
		super(handle, dev, lib -> lib::vkDestroyShaderModule);
	}
}
