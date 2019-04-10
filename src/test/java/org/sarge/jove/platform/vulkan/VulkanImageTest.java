package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

public class VulkanImageTest extends AbstractVulkanTest {
	@Test
	public void constructor() {
		final VkExtent3D extents = VulkanImage.extents(1, 2);
		final VulkanImage image = new VulkanImage(mock(Pointer.class), device, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, extents);
		assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, image.format());
		assertTrue(extents.dataEquals(image.extents()));
	}

	@Test
	public void builder() {
		final VulkanImage image = new VulkanImage.Builder(device)
			.type(VkImageType.VK_IMAGE_TYPE_3D)
			.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)
			.extents(VulkanImage.extents(1, 2, 3))
			.mipLevels(4)
			.arrayLayers(5)
			.tiling(VkImageTiling.VK_IMAGE_TILING_LINEAR)
			.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_GENERAL)
			.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
			.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
			.samples(VkSampleCountFlag.VK_SAMPLE_COUNT_2_BIT)
			.mode(VkSharingMode.VK_SHARING_MODE_CONCURRENT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT)
			.build();

		assertNotNull(image);
		assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, image.format());
		assertTrue(VulkanImage.extents(1, 2, 3).dataEquals(image.extents()));
	}
}
