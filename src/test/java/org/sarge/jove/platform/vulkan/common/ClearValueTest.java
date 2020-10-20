package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

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
		// Create clear colour
		final ClearValue clear = ClearValue.of(Colour.WHITE);
		assertNotNull(clear);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, clear.aspect());

		// Apply clear
		clear.populate(value);
		assertNotNull(value.color);
		assertArrayEquals(Colour.WHITE.toArray(), value.color.float32);

		// Equality
		assertEquals(true, clear.equals(clear));
		assertEquals(true, clear.equals(ClearValue.of(Colour.WHITE)));
		assertEquals(false, clear.equals(null));
		assertEquals(false, clear.equals(ClearValue.of(Colour.BLACK)));
		assertEquals(false, clear.equals(ClearValue.DEPTH));
	}

	@Test
	void defaultColour() {
		ClearValue.COLOUR.populate(value);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, ClearValue.COLOUR.aspect());
		assertArrayEquals(Colour.BLACK.toArray(), value.color.float32);
	}

	@Test
	void depth() {
		// Create depth clear
		final ClearValue clear = ClearValue.depth(0.5f);
		assertNotNull(clear);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, clear.aspect());

		// Apply clear
		clear.populate(value);
		assertNotNull(value.depthStencil);
		assertEquals(0.5f, value.depthStencil.depth);
		assertEquals(0, value.depthStencil.stencil);

		// Equality
		assertEquals(true, clear.equals(clear));
		assertEquals(true, clear.equals(ClearValue.depth(0.5f)));
		assertEquals(false, clear.equals(null));
		assertEquals(false, clear.equals(ClearValue.depth(1)));
		assertEquals(false, clear.equals(ClearValue.DEPTH));
	}

	@Test
	void defaultDepth() {
		ClearValue.DEPTH.populate(value);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, ClearValue.DEPTH.aspect());
		assertEquals(1, value.depthStencil.depth);
	}

	@Test
	void depthInvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> ClearValue.depth(2));
	}

	@Test
	void defaultColourAspect() {
		final ClearValue clear = ClearValue.of(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
		assertEquals(ClearValue.COLOUR, clear);
	}

	@Test
	void defaultDepthAspect() {
		final ClearValue clear = ClearValue.of(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT));
		assertEquals(ClearValue.DEPTH, clear);
	}

	@Test
	void defaultNone() {
		final ClearValue clear = ClearValue.of(Set.of());
		assertEquals(null, clear);
	}
}
