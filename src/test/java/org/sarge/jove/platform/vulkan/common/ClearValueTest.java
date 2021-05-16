package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.DepthClearValue;
import org.sarge.lib.util.Percentile;

public class ClearValueTest {
	private VkClearValue value;

	@BeforeEach
	void before() {
		value = new VkClearValue();
	}

	@Test
	void colour() {
		// Create colour clear value
		final ClearValue clear = new ColourClearValue(Colour.WHITE);
		assertEquals(VkImageAspect.COLOR, clear.aspect());

		// Apply clear
		clear.populate(value);
		assertNotNull(value.color);
		assertArrayEquals(Colour.WHITE.toArray(), value.color.float32);

		// Check equality
		assertEquals(true, clear.equals(clear));
		assertEquals(true, clear.equals(new ColourClearValue(Colour.WHITE)));
		assertEquals(false, clear.equals(null));
		assertEquals(false, clear.equals(new ColourClearValue(Colour.BLACK)));
	}

	@Test
	void depth() {
		// Create depth clear value
		final ClearValue clear = new DepthClearValue(Percentile.HALF);
		assertEquals(VkImageAspect.DEPTH, clear.aspect());

		// Apply clear
		clear.populate(value);
		assertNotNull(value.depthStencil);
		assertEquals(0.5f, value.depthStencil.depth);
		assertEquals(0, value.depthStencil.stencil);

		// Check equality
		assertEquals(true, clear.equals(clear));
		assertEquals(true, clear.equals(new DepthClearValue(Percentile.HALF)));
		assertEquals(false, clear.equals(null));
		assertEquals(false, clear.equals(new DepthClearValue(Percentile.ONE)));
	}

	@Test
	void defaultDepth() {
		assertEquals(new DepthClearValue(Percentile.ONE), DepthClearValue.DEFAULT);
	}

	@Test
	void none() {
		assertThrows(UnsupportedOperationException.class, () -> ClearValue.NONE.aspect());
		assertThrows(UnsupportedOperationException.class, () -> ClearValue.NONE.populate(null));
	}
}
