package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkRect2D;

/**
 * Vulkan utilities and helpers.
 * @author Sarge
 */
public final class VulkanUtility {
	private VulkanUtility() {
	}

	/**
	 * Populates a Vulkan rectangle.
	 * @param rect			Rectangle
	 * @param struct		Vulkan rectangle
	 */
	public static void populate(Rectangle rect, VkRect2D struct) {
		struct.offset.x = rect.x();
		struct.offset.y = rect.y();
		struct.extent.width = rect.width();
		struct.extent.height = rect.height();
	}
}
