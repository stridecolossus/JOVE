package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.*;

class SwapchainTest {
	private Swapchain swapchain;
	private View view;
	private LogicalDevice device;
	private MockSwapchainLibrary library;

	@BeforeEach
	void before() {
		library = new MockSwapchainLibrary();
		device = new MockLogicalDevice(library);
		view = new MockView(device);
		swapchain = new Swapchain(new Handle(2), device, List.of(view, new MockView(device)));
	}

	@Test
	void constructor() {
		assertEquals(2, swapchain.count());
		assertEquals(VkFormat.B8G8R8A8_UNORM, swapchain.format());
		assertEquals(new Dimensions(640, 480), swapchain.extents());
	}

	@Test
	void views() {
		assertEquals(view, swapchain.view(0));
		assertThrows(IndexOutOfBoundsException.class, () -> swapchain.view(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> swapchain.view(2));
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
		}

		@Test
		void suboptimal() {
			library.result = VkResult.VK_SUBOPTIMAL_KHR;
			assertEquals(0, swapchain.acquire(semaphore, null));
		}

		@Test
		void invalidated() {
			library.result = VkResult.VK_ERROR_OUT_OF_DATE_KHR;
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
			builder = new Builder();
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
			assertNotNull(swapchain.view(0));
		}

		// TODO
		// - count: zero, min/max, capabilities.min
		// - format: null, unsupported
		// - extent: null, min/max, capabilities.current
		// - others?
	}
}
