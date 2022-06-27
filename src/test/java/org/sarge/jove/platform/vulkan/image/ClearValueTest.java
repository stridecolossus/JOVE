package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.lib.util.Percentile;

public class ClearValueTest {
	private VkClearValue value;

	@BeforeEach
	void before() {
		value = new VkClearValue();
	}

	@Test
	void colour() {
		// Apply colour clear
		final ClearValue clear = new ColourClearValue(Colour.WHITE);
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
		// Apply depth clear
		final ClearValue clear = new DepthClearValue(Percentile.HALF);
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

	@SuppressWarnings("static-method")
	@Test
	void defaultDepth() {
		assertEquals(new DepthClearValue(Percentile.ONE), DepthClearValue.DEFAULT);
	}
}
