package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
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
		final var device = new MockLogicalDevice(); // mockery.proxy());

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

		builder = new Swapchain.Builder() {
			@Override
			public Swapchain build(LogicalDevice device, Properties properties) {
				return new MockSwapchain();
			}
		};

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
	void views() {
		assertNotNull(manager.views().apply(0));
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
