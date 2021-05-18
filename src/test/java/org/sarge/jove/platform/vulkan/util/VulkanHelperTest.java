package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkRect2D;

public class VulkanHelperTest {
	@Test
	void populate() {
		final VkRect2D rect = new VkRect2D();
		VulkanHelper.populate(new Rectangle(1, 2, 3, 4), rect);
		assertEquals(1, rect.offset.x);
		assertEquals(2, rect.offset.y);
		assertEquals(3, rect.extent.width);
		assertEquals(4, rect.extent.height);
	}

	@Test
	void buffer() {
		final ByteBuffer bb = VulkanHelper.buffer(42);
		assertNotNull(bb);
		assertEquals(42, bb.limit());
		assertEquals(42, bb.capacity());
		assertEquals(0, bb.position());
		assertEquals(ByteOrder.nativeOrder(), bb.order());
	}
}
