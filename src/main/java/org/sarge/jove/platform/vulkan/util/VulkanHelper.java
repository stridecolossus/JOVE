package org.sarge.jove.platform.vulkan.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkRect2D;

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
}
