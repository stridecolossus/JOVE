package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.util.Percentile;

class ClearValueTest {
	@Test
	void colour() {
		final var colour = new ColourClearValue(Colour.WHITE);
		final VkClearValue descriptor = ClearValue.populate(colour);
		assertArrayEquals(Colour.WHITE.toArray(), descriptor.color.float32);
	}

	@Test
	void depth() {
		final var depth = new DepthClearValue(Percentile.HALF);
		final VkClearValue descriptor = ClearValue.populate(depth);
		assertEquals(0.5f, descriptor.depthStencil.depth);
		assertEquals(0, descriptor.depthStencil.stencil);
	}
}
