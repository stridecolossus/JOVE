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
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Queue;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class SwapChainTest extends AbstractVulkanTest {
	private SwapChain chain;
	private View view;

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
		chain = new SwapChain(new Pointer(2), dev, VkFormat.VK_FORMAT_R8G8B8A8_UNORM, List.of(view));
	}

	@Test
	void constructor() {
		assertNotNull(chain.handle());
		assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, chain.format());
		assertEquals(new Dimensions(3, 4), chain.extents());
		assertEquals(List.of(view), chain.views());
	}

	@Test
	void acquire() {
		assertEquals(0, chain.acquire(null, null));
		verify(lib).vkAcquireNextImageKHR(eq(dev.handle()), eq(chain.handle()), eq(Long.MAX_VALUE), isNull(), isNull(), isA(IntByReference.class));
	}

	@Test
	void present() {
		// Present to queue
		final Queue queue = mock(Queue.class);
		when(queue.handle()).thenReturn(new Handle(new Pointer(42)));
		chain.present(queue, null);

		// Check API
		final ArgumentCaptor<VkPresentInfoKHR[]> captor = ArgumentCaptor.forClass(VkPresentInfoKHR[].class);
		verify(lib).vkQueuePresentKHR(eq(queue.handle()), captor.capture());

		// Check descriptors
		final VkPresentInfoKHR[] array = captor.getValue();
		assertNotNull(array);
		assertEquals(1, array.length);

		// Check descriptor
		final VkPresentInfoKHR info = array[0];
		assertNotNull(info);
		assertEquals(1, info.swapchainCount);
		assertNotNull(info.pSwapchains);
		assertNotNull(info.pImageIndices);
		// TODO - semaphore
	}

	@Test
	void destroy() {
		final Handle handle = chain.handle();
		chain.destroy();
		verify(lib).vkDestroySwapchainKHR(dev.handle(), handle, null);
	}

	@Nested
	class BuilderTests {
		private SwapChain.Builder builder;
		private Surface surface;
		private VkSurfaceCapabilitiesKHR caps;
		private VkExtent2D extent;

		@BeforeEach
		void before() {
			// Create surface
			surface = mock(Surface.class);
			when(surface.handle()).thenReturn(new Handle(new Pointer(42)));
			when(surface.device()).thenReturn(dev);
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
			builder = new SwapChain.Builder(surface);
		}

		@Test
		void build() {
			// Create chain
			chain = builder.format(VkFormat.VK_FORMAT_R8G8B8A8_UNORM).build();

			// Check swapchain
			assertNotNull(chain);
			assertNotNull(chain.handle());
			assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, chain.format());
			assertNotNull(chain.views());
			assertEquals(1, chain.views().size());

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
			assertEquals(extent, info.imageExtent);
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
			final View view = chain.views().get(0);
			assertNotNull(view);
			assertNotNull(view.handle());
			assertNotNull(view.image());
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
