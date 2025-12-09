package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan utilities.
 * @author Sarge
 */
public final class VulkanUtility {
	public VulkanUtility() {
	}

	/**
	 * Checks that the given size is aligned to four bytes.
	 * @param size Buffer offset or size
	 * @throws IllegalArgumentException if the given size is not a multiple of 4 bytes
	 */
	public static void checkAlignment(long size) {
		if((size % 4) != 0) {
			throw new IllegalArgumentException("Expected 4-byte alignment");
		}
	}

	public static VkExtent2D extent(Dimensions size) {
		final var extent = new VkExtent2D();
		extent.width = size.width();
		extent.height = size.height();
		return extent;
	}

	/**
	 * Converts a rectangle to the Vulkan equivalent.
	 * @param rectangle Rectangle
	 * @return Vulkan rectangle
	 */
	public static VkRect2D rectangle(Rectangle rectangle) {
		final VkRect2D result = new VkRect2D();
		result.offset = new VkOffset2D();
		result.offset.x = rectangle.x();
		result.offset.y = rectangle.y();
		result.extent = extent(rectangle.dimensions());
		return result;
	}
}
