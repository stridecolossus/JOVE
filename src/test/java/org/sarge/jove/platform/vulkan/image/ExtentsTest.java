package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;

class ExtentsTest {
	private Extents extents;

	@BeforeEach
	void before() {
		extents = new Extents(new Dimensions(2, 4), 3);
	}

	@Test
	void constructor() {
		assertEquals(new Dimensions(2, 4), extents.size());
		assertEquals(3, extents.depth());
	}

	@Test
	void extents() {
		final VkExtent3D result = extents.toExtent();
		assertEquals(2, result.width);
		assertEquals(4, result.height);
		assertEquals(3, result.depth);
	}

	@Test
	void offset() {
		final VkOffset3D result = extents.toOffset();
		assertEquals(2, result.x);
		assertEquals(4, result.y);
		assertEquals(3, result.z);
	}

	@Test
	void mip() {
		assertEquals(extents, extents.mip(0));
		assertEquals(new Extents(new Dimensions(1, 2), 3), extents.mip(1));
	}

	@Test
	void zero() {
		assertEquals(new Extents(new Dimensions(0, 0), 0), Extents.ZERO);
	}
}
