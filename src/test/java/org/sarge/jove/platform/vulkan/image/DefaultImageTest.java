package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.*;

class DefaultImageTest {
	@SuppressWarnings("unused")
	private class MockCreateImageLibrary extends MockLibrary {
		public VkResult vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Handle pAllocator, Pointer pImage) {
			assertEquals(VkStructureType.IMAGE_CREATE_INFO, pCreateInfo.sType);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(VkImageType.TYPE_2D, pCreateInfo.imageType);
			assertEquals(VkFormat.R32G32B32A32_SFLOAT, pCreateInfo.format);
			assertEquals(640, pCreateInfo.extent.width);
			assertEquals(480, pCreateInfo.extent.height);
			assertEquals(1, pCreateInfo.extent.depth);
			assertEquals(1, pCreateInfo.mipLevels);
			assertEquals(1, pCreateInfo.arrayLayers);
			assertEquals(new EnumMask<>(VkSampleCountFlags.COUNT_1), pCreateInfo.samples);
			assertEquals(VkImageTiling.OPTIMAL, pCreateInfo.tiling);
			assertEquals(VkImageLayout.PREINITIALIZED, pCreateInfo.initialLayout);
			assertEquals(new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT), pCreateInfo.usage);
			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.sharingMode);
			assertEquals(0, pCreateInfo.queueFamilyIndexCount);
			init(pImage);
			return VkResult.VK_SUCCESS;
		}

		public void vkGetImageMemoryRequirements(LogicalDevice device, Handle image, VkMemoryRequirements pMemoryRequirements) {
			pMemoryRequirements.size = 640 * 480 * 4;
		}

		public VkResult vkBindImageMemory(LogicalDevice device, Handle image, DeviceMemory memory, long memoryOffset) {
			assertEquals(640 * 480 * 4, memory.size());
			assertEquals(0, memoryOffset);
			return VkResult.VK_SUCCESS;
		}
	}

	private DefaultImage image;
	private Image.Descriptor descriptor;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockCreateImageLibrary(), Image.Library.class);

		descriptor = new Image.Descriptor.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.extents(new Dimensions(640, 480))
				.aspect(VkImageAspectFlags.COLOR)
				.build();

		final var properties = new MemoryProperties.Builder<VkImageUsageFlags>()
				.usage(VkImageUsageFlags.COLOR_ATTACHMENT)
				.build();

		image = new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(properties)
				.initialLayout(VkImageLayout.PREINITIALIZED)
				.build(new MockAllocator(new MockLogicalDevice(mockery.proxy())));
	}

	@Test
	void create() {
		assertEquals(descriptor, image.descriptor());
		assertEquals(640 * 480 * 4, image.memory().size());
		assertFalse(image.isDestroyed());
	}

	@Test
	void destroy() {
		image.destroy();
		assertTrue(image.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyImage").count());
	}
}
