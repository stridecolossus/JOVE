package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkSurfaceTransformFlagKHR.IDENTITY_KHR;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;
import org.sarge.jove.util.EnumMask;

public class SwapchainTest {
	static class MockSwapchainLibrary extends MockVulkanLibrary {
		private boolean concurrent;
		private boolean destroyed;
		private VkResult result = VkResult.SUCCESS;

		@Override
		public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
			assertNotNull(device);
			assertEquals(null, pAllocator);

			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(new Handle(3), pCreateInfo.surface);
			assertEquals(1, pCreateInfo.minImageCount);
			assertEquals(VkFormat.B8G8R8A8_UNORM, pCreateInfo.imageFormat);
			assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, pCreateInfo.imageColorSpace);
			assertEquals(640, pCreateInfo.imageExtent.width);
			assertEquals(480, pCreateInfo.imageExtent.height);
			assertEquals(1, pCreateInfo.imageArrayLayers);
			assertEquals(new EnumMask<>(VkImageUsageFlag.COLOR_ATTACHMENT), pCreateInfo.imageUsage);
			if(concurrent) {
    			assertEquals(VkSharingMode.CONCURRENT, pCreateInfo.imageSharingMode);
    			assertEquals(1, pCreateInfo.queueFamilyIndexCount);
			}
			else {
    			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.imageSharingMode);
    			assertEquals(0, pCreateInfo.queueFamilyIndexCount);
			}
			assertEquals(VkSurfaceTransformFlagKHR.IDENTITY_KHR, pCreateInfo.preTransform);
			assertEquals(VkCompositeAlphaFlagKHR.OPAQUE, pCreateInfo.compositeAlpha);
			assertEquals(VkPresentModeKHR.FIFO_KHR, pCreateInfo.presentMode);
			assertEquals(true, pCreateInfo.clipped);
			assertEquals(null, pCreateInfo.oldSwapchain);

			pSwapchain.set(new Handle(2));
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
			assertNotNull(device);
			assertEquals(null, pAllocator);
			pView.set(new Handle(3));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator) {
			assertNotNull(device);
			assertNotNull(swapchain);
			assertEquals(null, pAllocator);
			destroyed = true;
		}

		@Override
		public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
			assertNotNull(device);
			assertNotNull(swapchain);
			if(pSwapchainImages == null) {
				pSwapchainImageCount.set(1);
			}
			else {
				pSwapchainImages[0] = new Handle(4);
			}
			return VkResult.SUCCESS;
		}

		@Override
		public int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
			assertNotNull(device);
			assertNotNull(swapchain);
			assertEquals(Long.MAX_VALUE, timeout);
			pImageIndex.set(0);
			return result.value();
		}

		@Override
		public int vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo) {
			return result.value();
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
			pSurfaceCapabilities.currentExtent = new VkExtent2D();
			pSurfaceCapabilities.currentExtent.width = 640;
			pSurfaceCapabilities.currentExtent.height = 480;
			pSurfaceCapabilities.supportedTransforms = new EnumMask<>(IDENTITY_KHR);
			pSurfaceCapabilities.currentTransform = IDENTITY_KHR;
			pSurfaceCapabilities.maxImageArrayLayers = 1;
			pSurfaceCapabilities.minImageCount = 1;
			pSurfaceCapabilities.maxImageCount = 2;
			pSurfaceCapabilities.supportedUsageFlags = new EnumMask<>(VkImageUsageFlag.COLOR_ATTACHMENT);
			pSurfaceCapabilities.supportedCompositeAlpha = new EnumMask<>(VkCompositeAlphaFlagKHR.OPAQUE);
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkSurfaceFormatKHR[] formats) {
			if(formats == null) {
				count.set(1);
			}
			else {
				final var format = new VkSurfaceFormatKHR();
    			format.format = VkFormat.B8G8R8A8_UNORM;
    			format.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
    			formats[0] = format;
			}
			return VkResult.SUCCESS;
		}
	}

	private Swapchain swapchain;
	private View view;
	private LogicalDevice device;
	private MockSwapchainLibrary library;

	@BeforeEach
	void before() {
		library = new MockSwapchainLibrary();
		device = new MockLogicalDevice(library);
		// TODO...
		view = new View(new Handle(3), device, new MockImage()) {
			@Override
			protected Destructor<View> destructor() {
				return Destructor.empty();
			}
		};
		// ...TODO
		swapchain = new Swapchain(new Handle(2), device, library, VkFormat.B8G8R8A8_UNORM, new Dimensions(640, 480), List.of(view));
	}

	@Test
	void constructor() {
		assertEquals(VkFormat.B8G8R8A8_UNORM, swapchain.format());
		assertEquals(new Dimensions(640, 480), swapchain.extents());
		assertEquals(List.of(view), swapchain.attachments());
	}

	@Test
	void destroy() {
		swapchain.destroy();
		assertEquals(true, swapchain.isDestroyed());
		assertEquals(true, library.destroyed);
		assertEquals(true, view.isDestroyed());
	}

	@Nested
	class AcquireTest {
		private VulkanSemaphore semaphore;

		@BeforeEach
		void before() {
			semaphore = new MockVulkanSemaphore(device);
		}

		@Test
		void acquire() {
			assertEquals(0, swapchain.acquire(semaphore, null));
			assertEquals(view, swapchain.latest());
		}

		@Test
		void suboptimal() {
			library.result = VkResult.SUBOPTIMAL_KHR;
			assertEquals(0, swapchain.acquire(semaphore, null));
		}

		@Test
		void invalidated() {
			library.result = VkResult.ERROR_OUT_OF_DATE_KHR;
			assertThrows(SwapchainInvalidated.class, () -> swapchain.acquire(semaphore, null));
		}

		@Test
		void sync() {
			assertThrows(IllegalArgumentException.class, () -> swapchain.acquire(null, null));
		}

		@Test
		void latest() {
			assertEquals(view, swapchain.latest());
		}
	}

	@Nested
	class PresentTest {

	}

	@Nested
	class BuilderTest {
		private Swapchain.Builder builder;

		@BeforeEach
		void before() {
			final var surface = new MockVulkanSurface(library);
			builder = new Swapchain.Builder(surface.new Properties(new MockPhysicalDevice()));
		}

		@Test
		void build() {
			final var swapchain = builder.build(device);
			assertEquals(VkFormat.B8G8R8A8_UNORM, swapchain.format());
			assertEquals(new Dimensions(640, 480), swapchain.extents());
			assertEquals(1, swapchain.attachments().size());
		}

		@Test
		void concurrent() {
			final var family = new Family(1, 2, Set.of());
			library.concurrent = true;
			builder.concurrent(List.of(family)).build(device);
		}
	}
}
