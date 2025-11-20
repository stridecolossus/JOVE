package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;
import org.sarge.jove.platform.vulkan.render.SwapchainTest.MockSwapchainLibrary;

class SwapchainFactoryTest {
	private static class MockConfiguration implements SwapchainConfiguration {
		boolean applied;

		@Override
		public void configure(Builder builder, Properties properties) {
			applied = true;
		}
	}

	private SwapchainFactory factory;
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

		factory = new SwapchainFactory(device, properties, builder, List.of(configuration));
	}

	@Test
	void swapchain() {
		final Swapchain swapchain = factory.swapchain();
		assertEquals(false, swapchain.isDestroyed());
		assertEquals(true, configuration.applied);
	}

	@Test
	void recreate() {
		final Swapchain previous = factory.swapchain();
		factory.recreate();
		final Swapchain swapchain = factory.swapchain();
		assertEquals(false, swapchain.isDestroyed());
		assertNotSame(previous, swapchain);
		assertEquals(true, configuration.applied);
	}

	@Test
	void destroy() {
		final Swapchain swapchain = factory.swapchain();
		factory.destroy();
		assertEquals(true, swapchain.isDestroyed());
	}
}
