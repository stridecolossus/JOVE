package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

class ViewTest {
	private static class MockViewLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
			assertNotNull(device);
			assertEquals(null, pAllocator);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(new Handle(3), pCreateInfo.image);
			assertEquals(VkImageViewType.TWO_D, pCreateInfo.viewType);
			assertEquals(VkFormat.R32G32B32A32_SFLOAT, pCreateInfo.format);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.r);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.g);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.b);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.a);
			assertEquals(new EnumMask<>(VkImageAspect.COLOR), pCreateInfo.subresourceRange.aspectMask);
			assertEquals(0, pCreateInfo.subresourceRange.baseMipLevel);
			assertEquals(1, pCreateInfo.subresourceRange.levelCount);
			assertEquals(0, pCreateInfo.subresourceRange.baseArrayLayer);
			assertEquals(1, pCreateInfo.subresourceRange.layerCount);
			pView.set(MemorySegment.ofAddress(2));
			return VkResult.SUCCESS;
		}
	}

	private View view;
	private LogicalDevice device;
	private Image image;

	@BeforeEach
	void before() {
		device = new MockLogicalDevice(new MockViewLibrary());
		image = new MockImage();
		view = new View.Builder().build(device, image);
	}

	@Test
	void destroy() {
		view.destroy();
		assertEquals(true, view.isDestroyed());
	}
}
