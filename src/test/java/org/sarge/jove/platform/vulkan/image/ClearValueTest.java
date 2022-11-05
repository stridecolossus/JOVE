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

	@Nested
	class ColourTests {
		private ColourClearValue clear;

		@BeforeEach
		void before() {
			clear = new ColourClearValue(Colour.WHITE);
		}

		@Test
		void constructor() {
			assertEquals(Colour.WHITE, clear.colour());
			assertEquals(VkImageAspect.COLOR, clear.aspect());
		}

		@Test
		void populate() {
			clear.populate(value);
			assertArrayEquals(Colour.WHITE.toArray(), value.color.float32);
		}

		@Test
		void equals() {
			assertEquals(clear, clear);
			assertEquals(clear, new ColourClearValue(Colour.WHITE));
			assertNotEquals(clear, null);
			assertNotEquals(clear, new ColourClearValue(Colour.BLACK));
		}
	}

	@Nested
	class DepthStencilTests {
		private DepthClearValue clear;

		@BeforeEach
		void before() {
			clear = new DepthClearValue(Percentile.HALF);
		}

		@Test
		void constructor() {
			assertEquals(Percentile.HALF, clear.depth());
			assertEquals(VkImageAspect.DEPTH, clear.aspect());
		}

		@Test
		void populate() {
			clear.populate(value);
			assertEquals(0.5f, value.depthStencil.depth);
			assertEquals(0, value.depthStencil.stencil);
		}

		@Test
		void equals() {
			assertEquals(clear, clear);
			assertEquals(clear, new DepthClearValue(Percentile.HALF));
			assertNotEquals(clear, null);
			assertNotEquals(clear, DepthClearValue.DEFAULT);
		}

		@Test
		void def() {
			assertEquals(new DepthClearValue(Percentile.ONE), DepthClearValue.DEFAULT);
		}
	}
}
