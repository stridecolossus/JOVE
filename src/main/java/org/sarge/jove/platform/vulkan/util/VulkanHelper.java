package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkRect2D;

public final class VulkanHelper {
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
}
