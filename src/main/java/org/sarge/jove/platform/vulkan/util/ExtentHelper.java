package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.VkRect2D;

/**
 * Helpers for transforming JOVE domain objects to the equivalent Vulkan extent structures.
 * @author Sarge
 */
public final class ExtentHelper {
	private ExtentHelper() {
		// Utility class
	}

	/**
	 * Populates a Vulkan extent from the given dimensions.
	 * @param dim 		Dimensions
	 * @param extent	Extent
	 */
	public static void extent(Dimensions dim, VkExtent2D extent) {
		extent.width = dim.width();
		extent.height = dim.height();
	}

	/**
	 * Populates a Vulkan rectangle.
	 * @param rect		Rectangle
	 * @param out		Output Vulkan rectangle
	 */
	public static void rectangle(Rectangle rect, VkRect2D out) {
		out.offset.x = rect.x();
		out.offset.y = rect.y();
		out.extent.width = rect.width();
		out.extent.height = rect.height();
	}
}
