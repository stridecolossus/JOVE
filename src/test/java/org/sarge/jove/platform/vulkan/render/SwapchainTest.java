package org.sarge.jove.platform.vulkan.render;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

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
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
				.format(FORMAT)
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create swapchain image
		final Image image = mock(Image.class);
		when(image.handle()).thenReturn(new Handle(new Pointer(1)));
		when(image.descriptor()).thenReturn(descriptor);

		// Create view
		view = mock(View.class);
		when(view.image()).thenReturn(image);

		// Create swapchain
		swapchain = new Swapchain(new Pointer(2), dev, Swapchain.DEFAULT_FORMAT, List.of(view));

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
		assertEquals(Swapchain.DEFAULT_FORMAT, swapchain.format());
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
		final Queue queue = new Queue(new Handle(new Pointer(42)), dev, new Queue.Family(0, 1, Set.of()));
//
//				mock(Queue.class);
//		when(queue.handle()).thenReturn(new Handle(new Pointer(42)));
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

	@Test
	void format() {
		assertEquals(VkFormat.B8G8R8A8_UNORM, Swapchain.DEFAULT_FORMAT);
		// TODO - should be VkFormat.B8G8R8A8_SRGB; i.e. is it SRBG or UNORM?
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
			when(surface.handle()).thenReturn(new Handle(new Pointer(2)));
			when(surface.modes()).thenReturn(Set.of(VkPresentModeKHR.FIFO_KHR));

			// Init supported formats
			final VkSurfaceFormatKHR format = new VkSurfaceFormatKHR();
			format.format = Swapchain.DEFAULT_FORMAT;
			format.colorSpace = Swapchain.DEFAULT_COLOUR_SPACE;
			when(surface.formats()).thenReturn(Set.of(format));

			// Init surface capabilities descriptor
			caps = new VkSurfaceCapabilitiesKHR();
			caps.currentTransform = VkSurfaceTransformFlagKHR.IDENTITY_KHR;
			caps.minImageCount = 1;
			caps.maxImageCount = 1;
			caps.supportedTransforms = IntegerEnumeration.mask(VkSurfaceTransformFlagKHR.IDENTITY_KHR);
			caps.maxImageArrayLayers = 1;
			caps.supportedUsageFlags = IntegerEnumeration.mask(VkImageUsage.COLOR_ATTACHMENT);
			caps.supportedCompositeAlpha = IntegerEnumeration.mask(VkCompositeAlphaFlagKHR.OPAQUE);
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
					.clear(Colour.WHITE)
					.build();

			// Check swapchain
			assertNotNull(swapchain);
			assertNotNull(swapchain.handle());
			assertEquals(VkFormat.B8G8R8A8_UNORM, swapchain.format());
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
//			assertEquals(VkFormat.B8G8R8A8_SRGB, info.imageFormat);
			assertEquals(VkColorSpaceKHR.SRGB_NONLINEAR_KHR, info.imageColorSpace);

			assertNotNull(info.imageExtent);
			assertEquals(2, info.imageExtent.width);
			assertEquals(3, info.imageExtent.height);

			assertEquals(1, info.imageArrayLayers);
			assertEquals(VkImageUsage.COLOR_ATTACHMENT, info.imageUsage);
			assertEquals(VkSharingMode.EXCLUSIVE, info.imageSharingMode);
			assertEquals(0, info.queueFamilyIndexCount);
			assertEquals(null, info.pQueueFamilyIndices);
			assertEquals(VkSurfaceTransformFlagKHR.IDENTITY_KHR, info.preTransform);
			assertEquals(VkCompositeAlphaFlagKHR.OPAQUE, info.compositeAlpha);
			assertEquals(VkPresentModeKHR.FIFO_KHR, info.presentMode);
			assertEquals(VulkanBoolean.TRUE, info.clipped);
			assertEquals(null, info.oldSwapchain);

			// Check view allocation
			verify(lib).vkGetSwapchainImagesKHR(eq(dev.handle()), isA(Pointer.class), isA(IntByReference.class), isA(Pointer[].class));

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
			assertThrows(IllegalArgumentException.class, "Unsupported swapchain format", () -> builder.format(VkFormat.UNDEFINED).build());
		}

		@Test
		void invalidColourSpace() {
			builder.format(Swapchain.DEFAULT_FORMAT);
			assertThrows(IllegalArgumentException.class, "Unsupported swapchain format", () -> builder.space(VkColorSpaceKHR.ADOBERGB_LINEAR_EXT).build());
		}

		@Test
		void invalidArrayLayers() {
			assertThrows(IllegalArgumentException.class, () -> builder.arrays(0));
			assertThrows(IllegalArgumentException.class, () -> builder.arrays(2));
		}

		@Test
		void invalidImageUsage() {
			assertThrows(IllegalArgumentException.class, () -> builder.usage(VkImageUsage.DEPTH_STENCIL_ATTACHMENT));
		}

		@Test
		void invalidTransform() {
			assertThrows(IllegalArgumentException.class, () -> builder.transform(VkSurfaceTransformFlagKHR.HORIZONTAL_MIRROR_KHR));
		}

		@Test
		void invalidAlphaComposite() {
			assertThrows(IllegalArgumentException.class, () -> builder.alpha(VkCompositeAlphaFlagKHR.POST_MULTIPLIED));
		}

		@Test
		void invalidPresentationMode() {
			assertThrows(IllegalArgumentException.class, () -> builder.mode(VkPresentModeKHR.IMMEDIATE_KHR));
		}
	}
}
