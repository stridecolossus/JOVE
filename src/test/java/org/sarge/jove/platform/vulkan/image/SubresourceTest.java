package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

class SubresourceTest {
	private Subresource subresource;

	@BeforeEach
	void before() {
		subresource = new Subresource.Builder()
				.aspect(VkImageAspect.COLOR)
				.build();
	}

	@Test
	void builder() {
		assertEquals(Set.of(VkImageAspect.COLOR), subresource.aspects());
		assertEquals(0, subresource.mipLevel());
		assertEquals(1, subresource.levelCount());
		assertEquals(0, subresource.baseArrayLayer());
		assertEquals(1, subresource.layerCount());
	}

	@Test
	void range() {
		final VkImageSubresourceRange range = Subresource.range(subresource);
		assertEquals(new EnumMask<>(VkImageAspect.COLOR), range.aspectMask);
		assertEquals(0, range.baseMipLevel);
		assertEquals(1, range.levelCount);
		assertEquals(0, range.baseArrayLayer);
		assertEquals(1, range.layerCount);
	}

	@Test
	void layers() {
		final VkImageSubresourceLayers layers = Subresource.layers(subresource);
		assertEquals(new EnumMask<>(VkImageAspect.COLOR), layers.aspectMask);
		assertEquals(0, layers.mipLevel);
		assertEquals(0, layers.baseArrayLayer);
		assertEquals(1, layers.layerCount);
	}
}
