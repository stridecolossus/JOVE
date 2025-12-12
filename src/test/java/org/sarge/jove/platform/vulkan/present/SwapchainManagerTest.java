package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;

class SwapchainManagerTest {
	private static class MockConfiguration implements SwapchainConfiguration {
		boolean applied;

		@Override
		public void configure(Builder builder, Properties properties) {
			applied = true;
		}
	}

	private SwapchainManager manager;
	private MockSurfaceProperties properties;
	private Builder builder;
	private VkSurfaceFormatKHR format;
	private MockConfiguration configuration;

	@BeforeEach
	void before() {
		final var library = new MockSwapchainLibrary();
		final var device = new MockLogicalDevice(library);

		format = new SurfaceFormatWrapper(VkFormat.R32G32B32_SFLOAT, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);

		properties = new MockSurfaceProperties() {
			@Override
			public List<VkSurfaceFormatKHR> formats() {
				return List.of(format);
			}

			@Override
			public List<VkPresentModeKHR> modes() {
				return List.of(VkPresentModeKHR.FIFO_KHR);
			}
		};

		builder = new Builder()
				.count(1)
				.format(format)
				.extent(new Dimensions(640, 480));

		configuration = new MockConfiguration();

		manager = new SwapchainManager(device, properties, builder, List.of(configuration));
	}

	@Test
	void swapchain() {
		final Swapchain swapchain = manager.swapchain();
		assertEquals(false, swapchain.isDestroyed());
		assertEquals(true, configuration.applied);
	}

	@Test
	void recreate() {
		final Swapchain previous = manager.swapchain();
		manager.recreate();
		final Swapchain swapchain = manager.swapchain();
		assertEquals(false, swapchain.isDestroyed());
		assertNotSame(previous, swapchain);
		assertEquals(true, configuration.applied);
	}

	@Test
	void destroy() {
		final Swapchain swapchain = manager.swapchain();
		manager.destroy();
		assertEquals(true, swapchain.isDestroyed());
	}
}
