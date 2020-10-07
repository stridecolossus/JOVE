package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;

public class ImageSubResourceBuilderTest {
	private ImageSubResourceBuilder<Object> builder;
	private Object parent;

	@BeforeEach
	void before() {
		parent = new Object();
		builder = new ImageSubResourceBuilder<>(parent);
	}

	@Test
	void range() {
		// Build sub-resource range
		final var range = builder
				.mipLevel(1)
				.levelCount(2)
				.baseArrayLayer(3)
				.layerCount(4)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.range();

		// Check descriptor
		assertNotNull(range);
		assertEquals(1, range.baseMipLevel);
		assertEquals(2, range.levelCount);
		assertEquals(3, range.baseArrayLayer);
		assertEquals(4, range.layerCount);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), range.aspectMask);
	}

	@Test
	void layers() {
		// Build sub-resource layers
		final var range = builder
				.mipLevel(1)
				.baseArrayLayer(3)
				.layerCount(4)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.layers();

		// Check descriptor
		assertNotNull(range);
		assertEquals(1, range.mipLevel);
		assertEquals(3, range.baseArrayLayer);
		assertEquals(4, range.layerCount);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), range.aspectMask);
	}

	@Test
	void parent() {
		assertEquals(parent, builder.build());
	}
}
