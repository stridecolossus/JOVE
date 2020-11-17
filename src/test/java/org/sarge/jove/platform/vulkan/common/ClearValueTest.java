package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;

public class ClearValueTest {
	private VkClearValue value;

	@BeforeEach
	void before() {
		value = new VkClearValue();
	}

	@Test
	void colour() {
		// Create colour clear value
		final ClearValue clear = ClearValue.of(Colour.WHITE);
		assertNotNull(clear);
		assertEquals(true, clear.isValid(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));

		// Apply clear
		clear.populate(value);
		assertNotNull(value.color);
		assertArrayEquals(Colour.WHITE.toArray(), value.color.float32);
	}

	@Test
	void depth() {
		// Create depth clear value
		final ClearValue clear = ClearValue.depth(0.5f);
		assertNotNull(clear);
		assertEquals(true, clear.isValid(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT));

		// Apply clear
		clear.populate(value);
		assertNotNull(value.depthStencil);
		assertEquals(0.5f, value.depthStencil.depth);
		assertEquals(0, value.depthStencil.stencil);
	}

	@Test
	void none() {
		ClearValue.NONE.populate(null);
		assertEquals(true, ClearValue.NONE.isValid(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
		assertEquals(true, ClearValue.NONE.isValid(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT));
	}
}
