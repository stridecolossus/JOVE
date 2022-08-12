package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.render.Swapchain.*;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.IntegerArray;

import com.sun.jna.Pointer;

public class SwapchainTest extends AbstractVulkanTest {
	private Swapchain swapchain;
	private View view;
	private Dimensions extents;

	@BeforeEach
	void before() {
		view = mock(View.class);
		extents = new Dimensions(2, 3);
		swapchain = new Swapchain(new Pointer(1), dev, FORMAT, extents, List.of(view));
	}

	@Test
	void constructor() {
		assertEquals(FORMAT, swapchain.format());
		assertEquals(extents, swapchain.extents());
		assertEquals(List.of(view), swapchain.attachments());
	}

	@Test
	void destroy() {
		swapchain.destroy();
		verify(lib).vkDestroySwapchainKHR(dev, swapchain, null);
	}

	@Nested
	class AcquireFrameTests {
		private Semaphore semaphore;
		private Fence fence;

		@BeforeEach
		void before() {
			semaphore = mock(Semaphore.class);
			fence = mock(Fence.class);
		}

		@DisplayName("The next image to be rendered can be acquired from the swapchain")
		@Test
		void acquire() {
			when(lib.vkAcquireNextImageKHR(dev, swapchain, Long.MAX_VALUE, semaphore, fence, factory.integer())).thenReturn(VkResult.SUCCESS);
			assertEquals(1, swapchain.acquire(semaphore, fence));
		}

		@DisplayName("Acquiring the next image requires at least one synchronisation argument")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> swapchain.acquire(null, null));
		}

		@DisplayName("The next image cannot be acquired if the swapchain has become invalid")
		@Test
		void error() {
			when(lib.vkAcquireNextImageKHR(dev, swapchain, Long.MAX_VALUE, semaphore, null, factory.integer())).thenReturn(VkResult.ERROR_OUT_OF_DATE_KHR);
			assertThrows(SwapchainInvalidated.class, () -> swapchain.acquire(semaphore, null));
		}

		@DisplayName("The next image can be acquired if the swapchain is sub-optimal")
		@Test
		void suboptimal() {
			when(lib.vkAcquireNextImageKHR(dev, swapchain, Long.MAX_VALUE, null, fence, factory.integer())).thenReturn(VkResult.SUBOPTIMAL_KHR);
			swapchain.acquire(null, fence);
		}
	}

	@Nested
	class PresentationTests {
		private Queue queue;
		private Semaphore semaphore;

		@BeforeEach
		void before() {
			queue = new Queue(new Handle(2), new Family(0, 1, Set.of()));
			semaphore = mock(Semaphore.class);
			when(semaphore.handle()).thenReturn(new Handle(3));
		}

		@DisplayName("A rendered swapchain image can be presented to the swapchain")
		@Test
		void present() {
			final var expected = new VkPresentInfoKHR() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkPresentInfoKHR) obj;
					assertEquals(1, info.swapchainCount);
					assertEquals(NativeObject.array(List.of(swapchain)), info.pSwapchains);
					assertEquals(new IntegerArray(new int[]{4}), info.pImageIndices);
					assertEquals(1, info.waitSemaphoreCount);
					assertEquals(NativeObject.array(List.of(semaphore)), info.pWaitSemaphores);
					return true;
				}
			};
			when(lib.vkQueuePresentKHR(queue, expected)).thenReturn(VkResult.SUCCESS);
			swapchain.present(queue, 4, semaphore);
		}

		@DisplayName("A presentation task can be constructed by the builder")
		@Test
		void builder() {
			final VkPresentInfoKHR info = new PresentTaskBuilder()
					.image(swapchain, 4)
					.wait(semaphore)
					.build();

			assertNotNull(info);
			assertEquals(1, info.swapchainCount);
			assertEquals(NativeObject.array(List.of(swapchain)), info.pSwapchains);
			assertEquals(new IntegerArray(new int[]{4}), info.pImageIndices);
			assertEquals(1, info.waitSemaphoreCount);
			assertEquals(NativeObject.array(List.of(semaphore)), info.pWaitSemaphores);
		}

		@DisplayName("A presentation task cannot contain a duplicate swapchain")
		@Test
		void duplicate() {
			final var builder = new PresentTaskBuilder();
			builder.image(swapchain, 4);
			assertThrows(IllegalArgumentException.class, () -> builder.image(swapchain, 4));
		}
	}

	// TODO - wait ready?

	@Nested
	class BuilderTests {
		private Swapchain.Builder builder;
		private Surface surface;
		private VkSurfaceFormatKHR format;

		@BeforeEach
		void before() {
			// Init rendering surface
			format = Surface.defaultSurfaceFormat();
			surface = mock(Surface.class);
			when(surface.handle()).thenReturn(new Handle(1));
			when(surface.modes()).thenReturn(Set.of(VkPresentModeKHR.FIFO_KHR));
			when(surface.format(format.format, format.colorSpace)).thenReturn(Optional.of(format));

			// Init surface capabilities used by the swapchain
			final var caps = new VkSurfaceCapabilitiesKHR();
			caps.supportedTransforms = VkSurfaceTransformFlagKHR.IDENTITY_KHR.value();
			caps.currentTransform = VkSurfaceTransformFlagKHR.IDENTITY_KHR;
			caps.maxImageArrayLayers = 1;
			caps.supportedUsageFlags = VkImageUsageFlag.COLOR_ATTACHMENT.value();
			caps.supportedCompositeAlpha = VkCompositeAlphaFlagKHR.OPAQUE.value();
			when(surface.capabilities()).thenReturn(caps);

			// Init attachment extents
			extents = new Dimensions(3, 4);
			caps.currentExtent = new VkExtent2D();
			caps.currentExtent.width = extents.width();
			caps.currentExtent.height = extents.height();

			// Create builder
			builder = new Swapchain.Builder(dev, surface);
		}

		@DisplayName("A swapchain can be constructed with a default builder configuration")
		@Test
		void build() {
			// Create swapchain
			swapchain = builder.build();
			assertNotNull(swapchain);
			assertNotNull(swapchain.handle());
			assertEquals(false, swapchain.isDestroyed());
			assertEquals(dev, swapchain.device());
			assertEquals(format.format, swapchain.format());
			assertEquals(extents, swapchain.extents());

			// Check swapchain attachments
			assertNotNull(swapchain.attachments());
			assertEquals(1, swapchain.attachments().size());

			// Check colour attachment
			final View view = swapchain.attachments().get(0);
			assertNotNull(view);
			assertEquals(Optional.empty(), view.clear());
			assertEquals(false, view.isDestroyed());

			// Check colour image
			final Descriptor descriptor = new Descriptor.Builder()
					.format(format.format)
					.extents(extents)
					.aspect(VkImageAspect.COLOR)
					.build();
			final Image image = view.image();
			assertNotNull(image);
			assertEquals(descriptor, image.descriptor());

			// Check API
			final var expected = new VkSwapchainCreateInfoKHR() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkSwapchainCreateInfoKHR) obj;
					assertEquals(new Handle(1), info.surface);
					assertNotNull(info.imageExtent);
					assertEquals(VkSurfaceTransformFlagKHR.IDENTITY_KHR, info.preTransform);
					assertEquals(format.format, info.imageFormat);
					assertEquals(format.colorSpace, info.imageColorSpace);
					assertEquals(1, info.imageArrayLayers);
					assertEquals(VkSharingMode.EXCLUSIVE, info.imageSharingMode);
					assertEquals(VkImageUsageFlag.COLOR_ATTACHMENT, info.imageUsage);
					assertEquals(VkCompositeAlphaFlagKHR.OPAQUE, info.compositeAlpha);
					assertEquals(VkPresentModeKHR.FIFO_KHR, info.presentMode);
					assertEquals(VulkanBoolean.TRUE, info.clipped);
					return true;
				}
			};
			verify(lib).vkCreateSwapchainKHR(dev, expected, null, factory.pointer());
			verify(lib).vkGetSwapchainImagesKHR(dev, swapchain.handle().toPointer(), factory.integer(), new Pointer[1]);
		}

		@DisplayName("The swapchain format must be supported by the surface")
		@Test
		void format() {
			final var unsupported = new VkSurfaceFormatKHR();
			unsupported.format = VkFormat.UNDEFINED;
			unsupported.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
			assertThrows(IllegalArgumentException.class, () -> builder.format(unsupported));
		}

		@DisplayName("The presentation mode must be supported by the surface")
		@Test
		void mode() {
			assertThrows(IllegalArgumentException.class, () -> builder.presentation(VkPresentModeKHR.MAILBOX_KHR));
		}

		@DisplayName("The type of each attachment must be supported by the surface")
		@Test
		void usage() {
			assertThrows(IllegalArgumentException.class, () -> builder.usage(VkImageUsageFlag.DEPTH_STENCIL_ATTACHMENT));
		}
	}
}
