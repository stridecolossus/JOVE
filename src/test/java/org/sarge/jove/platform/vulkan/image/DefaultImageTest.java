package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.*;

class DefaultImageTest {
	private static class MockImageLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Handle pAllocator, Pointer pImage) {
			assertNotNull(device);
			pImage.set(MemorySegment.ofAddress(2));
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkGetImageMemoryRequirements(LogicalDevice device, Handle image, VkMemoryRequirements pMemoryRequirements) {
			assertEquals(new Handle(2), image);
			pMemoryRequirements.size = 640 * 480 * 4;
		}

		@Override
		public VkResult vkBindImageMemory(LogicalDevice device, Handle image, DeviceMemory memory, long memoryOffset) {
			assertEquals(new Handle(2), image);
			assertEquals(640 * 480 * 4, memory.size());
			assertEquals(0, memoryOffset);
			return VkResult.VK_SUCCESS;
		}
	}

	private DefaultImage image;
	private MockImageLibrary library;

	@BeforeEach
	void before() {
		library = new MockImageLibrary();
		final var device = new MockLogicalDevice(library);

		final var descriptor = new Image.Descriptor.Builder()
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
				.build(new MockAllocator(device));
	}

	@Test
	void destroy() {
		image.destroy();
		assertEquals(true, image.isDestroyed());
	}
}
