package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.VkOffset2D;
import org.sarge.jove.platform.vulkan.VkRect2D;

/**
 * Helpers for transforming JOVE domain objects to the equivalent Vulkan structures.
 * @author Sarge
 */
public final class ExtentHelper {
	private ExtentHelper() {
		// Utility class
	}

	/**
	 * Creates a Vulkan extent from the given dimensions.
	 * @param dim Dimensions
	 * @return New Vulkan extent
	 */
	public static VkExtent2D of(Dimensions dim){
		final VkExtent2D extent = new VkExtent2D();
		extent.width = dim.width();
		extent.height = dim.height();
		return extent;
	}

	/**
	 * Creates a Vulkan rectangle from the given rectangle.
	 * @param rect Rectangle
	 * @return New Vulkan rectangle
	 */
	public static VkRect2D of(Rectangle rect) {
		// Create offset
		final VkOffset2D offset = new VkOffset2D();
		offset.x = rect.x();
		offset.y = rect.y();

		// Create rectangle
		final VkRect2D result = new VkRect2D();
		result.offset = offset;
		result.extent = of(rect.size());

		return result;
	}
}
