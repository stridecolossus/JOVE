package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.*;

class ViewTest {
	static class MockViewLibrary extends MockLibrary {
		public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
			assertNotNull(device);
			assertEquals(null, pAllocator);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(new Handle(3), pCreateInfo.image);
			assertEquals(VkImageViewType.TYPE_2D, pCreateInfo.viewType);
			assertEquals(VkFormat.B8G8R8A8_UNORM, pCreateInfo.format);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.r);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.g);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.b);
			assertEquals(VkComponentSwizzle.IDENTITY, pCreateInfo.components.a);
			assertEquals(new EnumMask<>(VkImageAspectFlags.COLOR), pCreateInfo.subresourceRange.aspectMask);
			assertEquals(0, pCreateInfo.subresourceRange.baseMipLevel);
			assertEquals(1, pCreateInfo.subresourceRange.levelCount);
			assertEquals(0, pCreateInfo.subresourceRange.baseArrayLayer);
			assertEquals(1, pCreateInfo.subresourceRange.layerCount);
			pView.set(MemorySegment.ofAddress(2));
			return VkResult.VK_SUCCESS;
		}
	}

	private View view;
	private Image image;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockViewLibrary(), View.Library.class);
		image = new MockImage();
		view = new View.Builder().build(new MockLogicalDevice(mockery.proxy()), image);
	}

	@Test
	void constructor() {
		assertFalse(view.isDestroyed());
		assertEquals(image, view.image());
	}

	@Test
	void destroy() {
		view.destroy();
		assertTrue(view.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyImageView").count());
	}
}
