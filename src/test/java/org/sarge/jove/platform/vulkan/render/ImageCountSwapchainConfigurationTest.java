package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;

class ImageCountSwapchainConfigurationTest {
	private static class MockSwapchainBuilder extends Swapchain.Builder {
		int count;

		@Override
		public Builder count(int count) {
			this.count = count;
			return super.count(count);
		}
	}

	private MockSwapchainBuilder builder;
	private MockSurfaceProperties properties;

	@BeforeEach
	void before() {
		builder = new MockSwapchainBuilder();
		properties = new MockSurfaceProperties();
	}

	@Test
	void min() {
		final var configuration = new ImageCountSwapchainConfiguration(ImageCountSwapchainConfiguration.MIN);
		properties.capabilities.minImageCount = 2;
		configuration.configure(builder, properties);
		assertEquals(2, builder.count);
	}

	@Test
	void max() {
		final var configuration = new ImageCountSwapchainConfiguration(ImageCountSwapchainConfiguration.MAX);
		properties.capabilities.maxImageCount = 3;
		configuration.configure(builder, properties);
		assertEquals(3, builder.count);
	}
}
