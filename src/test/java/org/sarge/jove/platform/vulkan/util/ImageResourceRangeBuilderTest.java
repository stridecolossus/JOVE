package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;

public class ImageResourceRangeBuilderTest {
	private ImageResourceRangeBuilder<Object> builder;
	private Object parent;

	@BeforeEach
	void before() {
		parent = new Object();
		builder = new ImageResourceRangeBuilder<>(parent);
	}

	@Test
	void build() {
		// Build sub-resource range
		final var range = builder
				.baseMipLevel(1)
				.levelCount(2)
				.baseArrayLayer(3)
				.layerCount(4)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.result();

		// Check descriptor
		assertNotNull(range);
		assertEquals(1, range.baseMipLevel);
		assertEquals(2, range.levelCount);
		assertEquals(3, range.baseArrayLayer);
		assertEquals(4, range.layerCount);
		assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), range.aspectMask);
	}

	@Test
	void parent() {
		assertEquals(parent, builder.build());
	}
}
