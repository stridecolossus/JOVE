package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
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
		final ClearValue clear = new ColourClearValue(Colour.WHITE);
		clear.populate(value);
		assertNotNull(value.color);
		assertArrayEquals(Colour.WHITE.toArray(), value.color.float32);
		assertEquals(clear, clear);
		assertEquals(clear, new ColourClearValue(Colour.WHITE));
		assertNotEquals(clear, null);
		assertNotEquals(clear, new ColourClearValue(Colour.BLACK));
		assertEquals(VkImageAspect.COLOR, clear.aspect());
	}

	@Test
	void depth() {
		final ClearValue clear = new DepthClearValue(Percentile.HALF);
		clear.populate(value);
		assertNotNull(value.depthStencil);
		assertEquals(0.5f, value.depthStencil.depth);
		assertEquals(0, value.depthStencil.stencil);
		assertEquals(clear, clear);
		assertEquals(clear, new DepthClearValue(Percentile.HALF));
		assertNotEquals(clear, null);
		assertNotEquals(clear, DepthClearValue.DEFAULT);
		assertEquals(VkImageAspect.DEPTH, clear.aspect());
	}

	@Test
	void def() {
		assertEquals(new DepthClearValue(Percentile.ONE), DepthClearValue.DEFAULT);
	}
}
