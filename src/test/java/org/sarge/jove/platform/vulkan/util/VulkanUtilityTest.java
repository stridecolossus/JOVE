package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkRect2D;

class VulkanUtilityTest {
	@Test
	void rectangle() {
		final Rectangle rect = new Rectangle(1, 2, 3, 4);
		final VkRect2D struct = new VkRect2D();
		VulkanUtility.populate(rect, struct);
		assertEquals(1, struct.offset.x);
		assertEquals(2, struct.offset.y);
		assertEquals(3, struct.extent.width);
		assertEquals(4, struct.extent.height);
	}
}
