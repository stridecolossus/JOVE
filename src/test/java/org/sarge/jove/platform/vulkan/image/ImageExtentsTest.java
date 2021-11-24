package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkExtent3D;
import org.sarge.jove.platform.vulkan.VkOffset3D;

class ImageExtentsTest {
	private ImageExtents extents;

	@BeforeEach
	void before() {
		extents = new ImageExtents(2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, extents.depth());
		assertEquals(2, extents.size().width());
		assertEquals(3, extents.size().height());
	}

	@Test
	void extents() {
		final VkExtent3D result = extents.extents();
		assertNotNull(result);
		assertEquals(1, result.depth);
		assertEquals(2, result.width);
		assertEquals(3, result.height);
	}

	@Test
	void offset() {
		final VkOffset3D offset = extents.offsets();
		assertNotNull(offset);
		assertEquals(2, offset.x);
		assertEquals(3, offset.y);
		assertEquals(1, offset.z);
	}
}
