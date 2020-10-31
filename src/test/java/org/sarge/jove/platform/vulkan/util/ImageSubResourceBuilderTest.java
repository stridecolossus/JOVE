package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;

public class ImageSubResourceBuilderTest {
	private ImageSubResourceBuilder<Object> builder;
	private Object parent;

	@BeforeEach
	void before() {
		parent = new Object();
		builder = new ImageSubResourceBuilder<>(parent)
				.mipLevel(1)
				.levelCount(2)
				.baseArrayLayer(3)
				.layerCount(4)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT);
	}

	@Test
	void parent() {
		assertEquals(parent, builder.build());
	}

	@Test
	void range() {
		final var range = new VkImageSubresourceRange();
		builder.populate(range);
		assertNotNull(range);
		assertEquals(1, range.baseMipLevel);
		assertEquals(2, range.levelCount);
		assertEquals(3, range.baseArrayLayer);
		assertEquals(4, range.layerCount);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), range.aspectMask);
	}

	@Test
	void layers() {
		final var layers = new VkImageSubresourceLayers();
		builder.populate(layers);
		assertNotNull(layers);
		assertEquals(1, layers.mipLevel);
		assertEquals(3, layers.baseArrayLayer);
		assertEquals(4, layers.layerCount);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), layers.aspectMask);
	}
}
