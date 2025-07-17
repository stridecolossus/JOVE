package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.lib.Percentile;

public class ClearValueTest {
	private VkClearValue descriptor;

	@BeforeEach
	void before() {
		descriptor = new VkClearValue();
	}

	@Test
	void colour() {
		final ClearValue colour = new ColourClearValue(Colour.WHITE);
		assertEquals(VkImageAspect.COLOR, colour.aspect());
		colour.populate(descriptor);
		assertArrayEquals(Colour.WHITE.toArray(), descriptor.color.float32);
	}

	@Test
	void depth() {
		final ClearValue depth = new DepthClearValue(Percentile.HALF);
		assertEquals(VkImageAspect.DEPTH, depth.aspect());
		depth.populate(descriptor);
		assertEquals(0.5f, descriptor.depthStencil.depth);
		assertEquals(0, descriptor.depthStencil.stencil);
	}
}
