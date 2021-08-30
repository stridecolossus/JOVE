package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Set;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

/**
 * Vulkan helper and utility methods.
 * @author Sarge
 */
public final class VulkanHelper {
	private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

	private VulkanHelper() {
	}

	/**
	 * Populates a Vulkan rectangle.
	 * @param in		Rectangle
	 * @param out		Vulkan rectangle
	 */
	public static void populate(Rectangle in, VkRect2D out) {
		out.offset.x = in.x();
		out.offset.y = in.y();
		out.extent.width = in.width();
		out.extent.height = in.height();
	}

	/**
	 * Allocate a new byte buffer.
	 * @param len Buffer length
	 * @return New byte buffer
	 */
	public static ByteBuffer buffer(int len) {
		return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
	}

	/**
	 * Enumerates supported extensions.
	 * @param lib		Vulkan
	 * @param func 		Extensions function
	 * @return Supported extensions
	 */
	public static Set<String> extensions(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> func) {
		return Arrays
				.stream(VulkanFunction.enumerate(func, lib, VkExtensionProperties::new))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}
}
