package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Queue;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class SwapchainTest extends AbstractVulkanTest {
	private Swapchain swapchain;
	private View view;
	private Semaphore semaphore;
	private Fence fence;

	@BeforeEach
	void before() {
		// Specify image swapchain descriptor
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.extents(new Image.Extents(3, 4))
				.format(VkFormat.VK_FORMAT_R8G8B8A8_UNORM)
				.build();

		// Create swapchain image
		final Image image = mock(Image.class);
		when(image.handle()).thenReturn(new Handle(new Pointer(1)));
		when(image.descriptor()).thenReturn(descriptor);

		// Create view
		view = mock(View.class);
		when(view.image()).thenReturn(image);

		// Create swapchain
		swapchain = new Swapchain(new Pointer(2), dev, VkFormat.VK_FORMAT_R8G8B8A8_UNORM, List.of(view));

		// Create semaphore
		semaphore = mock(Semaphore.class);
		when(semaphore.handle()).thenReturn(new Handle(new Pointer(3)));

		// Create fence
		fence = mock(Fence.class);
		when(fence.handle()).thenReturn(new Handle(new Pointer(4)));
	}

	@Test
	void constructor() {
		assertNotNull(swapchain.handle());
		assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, swapchain.format());
		assertEquals(new Dimensions(3, 4), swapchain.extents());
		assertEquals(List.of(view), swapchain.views());
	}

	@Test
	void acquireSemaphore() {
		final Handle handle = semaphore.handle();
		assertEquals(0, swapchain.acquire(semaphore, null));
		verify(lib).vkAcquireNextImageKHR(eq(dev.handle()), eq(swapchain.handle()), eq(Long.MAX_VALUE), eq(handle), isNull(), isA(IntByReference.class));
	}

	@Test
	void acquireFence() {
		final Handle handle = fence.handle();
		swapchain.acquire(null, fence);
		verify(lib).vkAcquireNextImageKHR(eq(dev.handle()), eq(swapchain.handle()), eq(Long.MAX_VALUE), isNull(), eq(handle), isA(IntByReference.class));
	}

	@Test
	void acquireBoth() {
		swapchain.acquire(semaphore, fence);
	}

	@Test
	void acquireNeither() {
		assertThrows(IllegalArgumentException.class, () -> swapchain.acquire(null, null));
	}

	@Test
	void present() {
		// Present to queue
		final Queue queue = mock(Queue.class);
		when(queue.handle()).thenReturn(new Handle(new Pointer(42)));
		swapchain.present(queue, Set.of(semaphore));

		// Check API
		final ArgumentCaptor<VkPresentInfoKHR> captor = ArgumentCaptor.forClass(VkPresentInfoKHR.class);
		verify(lib).vkQueuePresentKHR(eq(queue.handle()), captor.capture());

		// Check descriptor
		final VkPresentInfoKHR info = captor.getValue();
		assertNotNull(info);

		// Check swapchain
		assertEquals(1, info.swapchainCount);
		assertNotNull(info.pSwapchains);
		assertNotNull(info.pImageIndices);

		// Check semaphores
		assertEquals(1, info.waitSemaphoreCount);
		assertNotNull(info.pWaitSemaphores);
	}

	@Test
	void destroy() {
		final Handle handle = swapchain.handle();
		swapchain.destroy();
		verify(lib).vkDestroySwapchainKHR(dev.handle(), handle, null);
		verify(view).destroy();
	}

	@Nested
	class BuilderTests {
		private Swapchain.Builder builder;
		private Surface surface;
		private VkSurfaceCapabilitiesKHR caps;
		private VkExtent2D extent;

		@BeforeEach
		void before() {
			// Create surface
			surface = mock(Surface.class);
			when(surface.handle()).thenReturn(new Handle(new Pointer(42)));
			when(surface.modes()).thenReturn(Set.of(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR));

			// Init supported formats
			final VkSurfaceFormatKHR format = new VkSurfaceFormatKHR();
			format.format = VkFormat.VK_FORMAT_R8G8B8A8_UNORM;
			format.colorSpace = VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
			when(surface.formats()).thenReturn(Set.of(format));

			// Init surface capabilities descriptor
			caps = new VkSurfaceCapabilitiesKHR();
			caps.currentTransform = VkSurfaceTransformFlagKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
			caps.minImageCount = 1;
			caps.maxImageCount = 1;
			caps.supportedTransforms = IntegerEnumeration.mask(VkSurfaceTransformFlagKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR);
			caps.maxImageArrayLayers = 1;
			caps.supportedUsageFlags = IntegerEnumeration.mask(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			caps.supportedCompositeAlpha = IntegerEnumeration.mask(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			when(surface.capabilities()).thenReturn(caps);

			// Init surface extents
			extent = new VkExtent2D();
			extent.width = 2;
			extent.height = 3;
			caps.currentExtent = extent;

			// Create builder
			builder = new Swapchain.Builder(dev, surface);
		}

		@Test
		void build() {
			// Create chain
			swapchain = builder
					.format(VkFormat.VK_FORMAT_R8G8B8A8_UNORM)
					.clear(Colour.WHITE)
					.build();

			// Check swapchain
			assertNotNull(swapchain);
			assertNotNull(swapchain.handle());
			assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, swapchain.format());
			assertNotNull(swapchain.views());
			assertEquals(1, swapchain.views().size());

			// Check allocation
			final ArgumentCaptor<VkSwapchainCreateInfoKHR> captor = ArgumentCaptor.forClass(VkSwapchainCreateInfoKHR.class);
			verify(lib).vkCreateSwapchainKHR(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

			// Check descriptor
			final VkSwapchainCreateInfoKHR info = captor.getValue();
			assertNotNull(info);
			assertEquals(surface.handle(), info.surface);
			assertEquals(1, info.minImageCount);
			assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, info.imageFormat);
			assertEquals(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR, info.imageColorSpace);

			assertNotNull(info.imageExtent);
			assertEquals(2, info.imageExtent.width);
			assertEquals(3, info.imageExtent.height);

			assertEquals(1, info.imageArrayLayers);
			assertEquals(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, info.imageUsage);
			assertEquals(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, info.imageSharingMode);
			assertEquals(0, info.queueFamilyIndexCount);
			assertEquals(null, info.pQueueFamilyIndices);
			assertEquals(VkSurfaceTransformFlagKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR, info.preTransform);
			assertEquals(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR, info.compositeAlpha);
			assertEquals(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR, info.presentMode);
			assertEquals(VulkanBoolean.TRUE, info.clipped);
			assertEquals(null, info.oldSwapchain);

			// Check view allocation
			verify(lib).vkGetSwapchainImagesKHR(eq(dev.handle()), eq(factory.ptr.getValue()), isA(IntByReference.class), isA(Pointer[].class));

			// Check view
			final View view = swapchain.views().get(0);
			assertNotNull(view);
			assertNotNull(view.handle());
			assertNotNull(view.image());
//			assertEquals(clear, view.clear());
		}

		@Test
		void invalidExtents() {
			assertThrows(IllegalArgumentException.class, () -> builder.extent(new Dimensions(1, 2)));
			assertThrows(IllegalArgumentException.class, () -> builder.extent(new Dimensions(3, 4)));
		}

		@Test
		void invalidImageCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.count(0));
			assertThrows(IllegalArgumentException.class, () -> builder.count(2));
		}

		@Test
		void invalidFormat() {
			assertThrows(IllegalArgumentException.class, () -> builder.format(VkFormat.VK_FORMAT_A1R5G5B5_UNORM_PACK16));
		}

		@Test
		void invalidColourSpace() {
			assertThrows(IllegalArgumentException.class, () -> builder.space(VkColorSpaceKHR.VK_COLOR_SPACE_ADOBERGB_LINEAR_EXT));
		}

		@Test
		void invalidArrayLayers() {
			assertThrows(IllegalArgumentException.class, () -> builder.arrays(0));
			assertThrows(IllegalArgumentException.class, () -> builder.arrays(2));
		}

		@Test
		void invalidImageUsage() {
			assertThrows(IllegalArgumentException.class, () -> builder.usage(VkImageUsageFlag.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT));
		}

		@Test
		void invalidTransform() {
			assertThrows(IllegalArgumentException.class, () -> builder.transform(VkSurfaceTransformFlagKHR.VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_BIT_KHR));
		}

		@Test
		void invalidAlphaComposite() {
			assertThrows(IllegalArgumentException.class, () -> builder.alpha(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR));
		}

		@Test
		void invalidPresentationMode() {
			assertThrows(IllegalArgumentException.class, () -> builder.present(VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR));
		}
	}
}
