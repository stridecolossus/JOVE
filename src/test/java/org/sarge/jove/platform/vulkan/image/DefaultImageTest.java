package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

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
			pImage.set(new Handle(2));
			return VkResult.SUCCESS;
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
			return VkResult.SUCCESS;
		}
	}

	private DefaultImage image;
	private LogicalDevice device;
	private MockImageLibrary library;

	@BeforeEach
	void before() {
		library = new MockImageLibrary();
		device = new MockLogicalDevice(library);

		final var descriptor = new Image.Descriptor.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.extents(new Dimensions(640, 480))
				.aspect(VkImageAspect.COLOR)
				.build();

		final var properties = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
				.build();

		image = new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(properties)
				.initialLayout(VkImageLayout.PREINITIALIZED)
				.build(device, new MockAllocator());
	}

	@Test
	void destroy() {
		image.destroy();
		assertEquals(true, image.isDestroyed());
	}
}
