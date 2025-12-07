package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.render.ImageCountSwapchainConfiguration.Policy;
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

	private ImageCountSwapchainConfiguration configuration;
	private MockSwapchainBuilder builder;

	@BeforeEach
	void before() {
		builder = new MockSwapchainBuilder();
		configuration = new ImageCountSwapchainConfiguration(_ -> 3);
	}

	@Test
	void configure() {
		configuration.configure(builder, new MockSurfaceProperties());
		assertEquals(3, builder.count);
	}

	@Test
	void policy() {
		final var capabilities = new VkSurfaceCapabilitiesKHR();
		capabilities.minImageCount = 2;
		capabilities.maxImageCount = 4;
		assertEquals(2, Policy.MIN.applyAsInt(capabilities));
		assertEquals(3, Policy.MIN_PLUS_ONE.applyAsInt(capabilities));
		assertEquals(4, Policy.MAX.applyAsInt(capabilities));
	}
}
