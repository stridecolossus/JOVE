package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkExtent3D;

class ImageExtentsTest {
	private ImageExtents extents;

	@BeforeEach
	void before() {
		extents = new ImageExtents(2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, extents.depth());
		assertEquals(2, extents.dimensions().width());
		assertEquals(3, extents.dimensions().height());
	}

	@Test
	void toExtents3D() {
		final VkExtent3D result = extents.toExtent3D();
		assertNotNull(result);
		assertEquals(1, result.depth);
		assertEquals(2, result.width);
		assertEquals(3, result.height);
	}
}
