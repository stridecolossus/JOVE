package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;

class ExtentSwapchainConfigurationTest {
	private static class MockSwapchainBuilder extends Swapchain.Builder {
		VkExtent2D extent;

		@Override
		public Builder extent(VkExtent2D extent) {
			this.extent = extent;
			return super.extent(extent);
		}
	}

	private ExtentSwapchainConfiguration configuration;
	private MockSwapchainBuilder builder;
	private MockSurfaceProperties properties;

	@BeforeEach
	void before() {
		final var surface = new MockVulkanSurface() {
			@Override
			public Window window() {
				return new MockWindow() {
					@Override
					public Dimensions size(Window.Unit unit) {
						return new Dimensions(1024, 768);
					}
				};
			}
		};
		properties = new MockSurfaceProperties() {
			@Override
			public VulkanSurface surface() {
				return surface;
			}
		};
		builder = new MockSwapchainBuilder();
		configuration = new ExtentSwapchainConfiguration();
	}

	@Test
	void current() {
		properties.capabilities.currentExtent = Vulkan.extent(new Dimensions(640, 480));
		configuration.configure(builder, properties);
		assertEquals(640, builder.extent.width);
		assertEquals(480, builder.extent.height);
	}

	@Test
	void framebuffer() {
		properties.capabilities.currentExtent.width = Integer.MAX_VALUE;
		configuration.configure(builder, properties);
		assertEquals(1024, builder.extent.width);
		assertEquals(768, builder.extent.height);
	}

	@Test
	void clamp() {
		properties.capabilities.maxImageExtent.width = 800;
		properties.capabilities.maxImageExtent.height = 600;
		configuration.configure(builder, properties);
		assertEquals(800, builder.extent.width);
		assertEquals(600, builder.extent.height);
	}
}
