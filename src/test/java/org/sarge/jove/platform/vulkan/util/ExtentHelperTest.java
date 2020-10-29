package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.VkRect2D;

public class ExtentHelperTest {
	@Test
	void extent() {
		final VkExtent2D extent = new VkExtent2D();
		ExtentHelper.extent(new Dimensions(3, 4), extent);
		assertEquals(3, extent.width);
		assertEquals(4, extent.height);
	}

	@Test
	void rectangle() {
		final VkRect2D rect = new VkRect2D();
		ExtentHelper.rectangle(new Rectangle(1, 2, 3, 4), rect);
		assertEquals(1, rect.offset.x);
		assertEquals(2, rect.offset.y);
		assertEquals(3, rect.extent.width);
		assertEquals(4, rect.extent.height);
	}
}
