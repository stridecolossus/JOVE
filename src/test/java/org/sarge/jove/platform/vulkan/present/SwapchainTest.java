package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.*;
import org.sarge.jove.util.*;
import org.sarge.jove.util.Mockery.Mock;

class SwapchainTest {
	@SuppressWarnings("unused")
	private static class MockSwapchainLibrary extends MockLibrary {
		public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
			assertEquals(VkStructureType.SWAPCHAIN_CREATE_INFO_KHR, pCreateInfo.sType);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertNotNull(pCreateInfo.surface);
			assertEquals(new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT), pCreateInfo.imageUsage);
			assertTrue(pCreateInfo.minImageCount > 0);
			assertEquals(VkFormat.B8G8R8A8_UNORM, pCreateInfo.imageFormat);
			assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, pCreateInfo.imageColorSpace);
			assertTrue(pCreateInfo.imageExtent.width >= 640);
			assertTrue(pCreateInfo.imageExtent.width <= 1024);
			assertTrue(pCreateInfo.imageExtent.height >= 480);
			assertTrue(pCreateInfo.imageExtent.height <= 768);
			assertEquals(1, pCreateInfo.imageArrayLayers);
			assertEquals(new EnumMask<>(VkImageUsageFlags.COLOR_ATTACHMENT), pCreateInfo.imageUsage);
			assertEquals(new EnumMask<>(VkSurfaceTransformFlagsKHR.IDENTITY_KHR), pCreateInfo.preTransform);
			assertEquals(new EnumMask<>(VkCompositeAlphaFlagsKHR.OPAQUE_KHR), pCreateInfo.compositeAlpha);
			assertNotNull(pCreateInfo.presentMode);
			assertEquals(true, pCreateInfo.clipped);
			assertEquals(null, pCreateInfo.oldSwapchain);
			init(pSwapchain);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
			pSwapchainImageCount.set(1);
			init(pSwapchainImages);
			return VkResult.VK_SUCCESS;
		}

		public int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
			assertEquals(Long.MAX_VALUE, timeout);
			pImageIndex.set(0);
			return result.value();
		}
	}

	private Swapchain swapchain;
	private View view;
	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockSwapchainLibrary(), Swapchain.Library.class, View.Library.class);
		device = new MockLogicalDevice(mockery.proxy());
		view = new MockView();
		swapchain = new Swapchain(new Handle(2), device, List.of(view, new MockView()));
	}

	@Test
	void constructor() {
		assertEquals(VkFormat.B8G8R8A8_UNORM, swapchain.format());
		assertEquals(new Dimensions(640, 480), swapchain.extents());
	}

	@Test
	void attachments() {
		assertEquals(2, swapchain.attachments());
		assertEquals(view, swapchain.attachment(0));
		assertThrows(IndexOutOfBoundsException.class, () -> swapchain.attachment(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> swapchain.attachment(2));
	}

	@DisplayName("The number of in-flight frames is the same as the number of colour attachments by default")
	@Test
	void frames() {
		assertEquals(2, swapchain.frames());
	}

	@Test
	void destroy() {
		swapchain.destroy();
		assertTrue(swapchain.isDestroyed());
		assertTrue(view.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroySwapchainKHR").count());
	}

	@Nested
	class AcquireTest {
		private VulkanSemaphore semaphore;
		private Mock acquire;

		@BeforeEach
		void before() {
			semaphore = new MockVulkanSemaphore();
			acquire = mockery.mock("vkAcquireNextImageKHR");
		}

		@Test
		void acquire() {
			assertEquals(0, swapchain.acquire(semaphore, null));
		}

		@Test
		void suboptimal() {
			acquire.result(VkResult.VK_SUBOPTIMAL_KHR.value());
			assertEquals(0, swapchain.acquire(semaphore, null));
		}

		@Test
		void invalidated() {
			acquire.result(VkResult.VK_ERROR_OUT_OF_DATE_KHR.value());
			assertThrows(Invalidated.class, () -> swapchain.acquire(semaphore, null));
		}

		@Test
		void sync() {
			assertThrows(IllegalArgumentException.class, () -> swapchain.acquire(null, null));
		}
	}

	@Nested
	class BuilderTest {
		private Builder builder;
		private MockSurfaceProperties properties;

		@BeforeEach
		void before() {
			properties = new MockSurfaceProperties();

			builder = new Swapchain.Builder() {
				@Override
				protected List<View> attachments(LogicalDevice device, Handle swapchain) {
					return List.of(new MockView());
				}
			};
		}

		@Test
		void build() {
			final Swapchain swapchain = builder
					.count(1)
					.format(MockSurfaceProperties.FORMAT)
					.extent(new Dimensions(640, 480))
					.build(device, properties);

			assertEquals(new Dimensions(640, 480), swapchain.extents());
			assertEquals(MockSurfaceProperties.FORMAT.format, swapchain.format());
			assertEquals(false, swapchain.isDestroyed());
			assertNotNull(swapchain.attachment(0));
		}

		// TODO
		// - count: zero, min/max, capabilities.min
		// - format: null, unsupported
		// - extent: null, min/max, capabilities.current
		// - others?
	}
}
