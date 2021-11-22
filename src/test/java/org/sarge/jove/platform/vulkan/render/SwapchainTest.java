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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor;
import org.sarge.jove.platform.vulkan.image.ImageExtents;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class SwapchainTest extends AbstractVulkanTest {
	private Swapchain swapchain;
	private View view;
	private Semaphore semaphore;
	private Fence fence;

	@BeforeEach
	void before() {
		// Specify image swapchain descriptor
		final Dimensions extents = new Dimensions(3, 4);
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.extents(new ImageExtents(extents))
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
		swapchain = new Swapchain(new Pointer(2), dev, Swapchain.DEFAULT_FORMAT, extents, List.of(view));

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
		assertEquals(1, swapchain.count());
	}

	@Test
	void acquireSemaphore() {
		assertEquals(1, swapchain.acquire(semaphore, null));
		verify(lib).vkAcquireNextImageKHR(dev, swapchain, Long.MAX_VALUE, semaphore, null, INTEGER);
	}

	@Test
	void acquireFence() {
		swapchain.acquire(null, fence);
		verify(lib).vkAcquireNextImageKHR(dev, swapchain, Long.MAX_VALUE, null, fence, INTEGER);
	}

	@Test
	void acquireBoth() {
		swapchain.acquire(semaphore, fence);
	}

	@Test
	void acquireNeither() {
		assertThrows(IllegalArgumentException.class, () -> swapchain.acquire(null, null));
	}

	@DisplayName("The swapchain should wait for a previous frame to be completed")
	@Test
	void waitReady() {
		swapchain.waitReady(0, fence);
		swapchain.waitReady(0, null);
		verify(fence).waitReady();
	}

	@Test
	void present() {
		// Present to queue
		final Queue queue = new Queue(new Handle(new Pointer(42)), new Queue.Family(0, 1, Set.of()));
//
//				mock(Queue.class);
//		when(queue.handle()).thenReturn(new Handle(new Pointer(42)));
		swapchain.present(queue, 0, Set.of(semaphore));

		// Check API
		final ArgumentCaptor<VkPresentInfoKHR> captor = ArgumentCaptor.forClass(VkPresentInfoKHR.class);
		verify(lib).vkQueuePresentKHR(eq(queue), captor.capture());

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
		swapchain.destroy();
		verify(lib).vkDestroySwapchainKHR(dev, swapchain, null);
		verify(view).destroy();
	}

	@Test
	void format() {
		assertEquals(VkFormat.B8G8R8A8_UNORM, Swapchain.DEFAULT_FORMAT);
		// TODO - should be VkFormat.B8G8R8A8_SRGB; i.e. is it SRBG or UNORM?
	}

	@Test
	void mode() {
		// Check default mode
		final var props = mock(Surface.Properties.class);
		assertEquals(VkPresentModeKHR.FIFO_KHR, Swapchain.mode(props, VkPresentModeKHR.MAILBOX_KHR));

		// Select supported mode
		when(props.modes()).thenReturn(Set.of(VkPresentModeKHR.MAILBOX_KHR));
		assertEquals(VkPresentModeKHR.MAILBOX_KHR, Swapchain.mode(props, VkPresentModeKHR.MAILBOX_KHR));
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
//			when(surface.handle()).thenReturn(new Handle(new Pointer(2)));

			// Init supported presentation modes
			final Surface.Properties props = mock(Surface.Properties.class);
			when(props.modes()).thenReturn(Set.of(VkPresentModeKHR.FIFO_KHR));
			when(props.surface()).thenReturn(surface);
//			when(props.device()).thenReturn(null)

			// Init supported formats
			final VkSurfaceFormatKHR format = new VkSurfaceFormatKHR();
			format.format = Swapchain.DEFAULT_FORMAT;
			format.colorSpace = Swapchain.DEFAULT_COLOUR_SPACE;
			when(props.formats()).thenReturn(List.of(format));

			// Init surface capabilities descriptor
			caps = new VkSurfaceCapabilitiesKHR();
			caps.currentTransform = VkSurfaceTransformFlagKHR.IDENTITY_KHR;
			caps.minImageCount = 1;
			caps.maxImageCount = 1;
			caps.supportedTransforms = IntegerEnumeration.mask(VkSurfaceTransformFlagKHR.IDENTITY_KHR);
			caps.maxImageArrayLayers = 1;
			caps.supportedUsageFlags = IntegerEnumeration.mask(VkImageUsage.COLOR_ATTACHMENT);
			caps.supportedCompositeAlpha = IntegerEnumeration.mask(VkCompositeAlphaFlagKHR.OPAQUE);
			when(props.capabilities()).thenReturn(caps);

			// Init surface extents
			extent = new VkExtent2D();
			extent.width = 2;
			extent.height = 3;
			caps.currentExtent = extent;

			// Create builder
			builder = new Swapchain.Builder(dev, props);
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
			verify(lib).vkCreateSwapchainKHR(eq(dev), captor.capture(), isNull(), eq(POINTER));

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
			verify(lib).vkGetSwapchainImagesKHR(eq(dev), isA(Pointer.class), isA(IntByReference.class), isA(Pointer[].class));

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
			assertThrows(IllegalArgumentException.class, "Unsupported surface format", () -> builder.format(VkFormat.UNDEFINED).build());
		}

		@Test
		void invalidColourSpace() {
			builder.format(Swapchain.DEFAULT_FORMAT);
			assertThrows(IllegalArgumentException.class, "Unsupported surface format", () -> builder.space(VkColorSpaceKHR.ADOBERGB_LINEAR_EXT).build());
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
			assertThrows(IllegalArgumentException.class, () -> builder.presentation(VkPresentModeKHR.IMMEDIATE_KHR));
		}
	}
}
