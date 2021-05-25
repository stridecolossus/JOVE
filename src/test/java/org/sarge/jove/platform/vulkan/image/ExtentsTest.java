package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkExtent3D;

class ExtentsTest {
	private Extents extents;

	@BeforeEach
	void before() {
		extents = new Extents(2, 3);
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
		assertEquals(1, result.depth);
		assertEquals(2, result.width);
		assertEquals(3, result.height);
	}
}
