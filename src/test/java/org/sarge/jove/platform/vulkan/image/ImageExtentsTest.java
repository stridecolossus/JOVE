package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkExtent3D;
import org.sarge.jove.platform.vulkan.VkOffset3D;

class ImageExtentsTest {
	private ImageExtents extents;

	@BeforeEach
	void before() {
		extents = new ImageExtents(640, 480);
	}

	@Test
	void constructor() {
		assertEquals(1, extents.depth());
		assertEquals(640, extents.size().width());
		assertEquals(480, extents.size().height());
	}

	@Test
	void mip() {
		assertEquals(new ImageExtents(new Dimensions(320, 240)), extents.mip(1));
		assertEquals(extents, extents.mip(0));
	}

	@Test
	void extents() {
		final VkExtent3D result = extents.extents();
		assertNotNull(result);
		assertEquals(1, result.depth);
		assertEquals(640, result.width);
		assertEquals(480, result.height);
	}

	@Test
	void offset() {
		final VkOffset3D offset = extents.offsets();
		assertNotNull(offset);
		assertEquals(640, offset.x);
		assertEquals(480, offset.y);
		assertEquals(1, offset.z);
	}
}
