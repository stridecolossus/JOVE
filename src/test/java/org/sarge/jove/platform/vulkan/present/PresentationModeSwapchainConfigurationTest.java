package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;

class PresentationModeSwapchainConfigurationTest {
	private static class MockSwapchainBuilder extends Swapchain.Builder {
		VkPresentModeKHR selected;

		@Override
		public Builder presentation(VkPresentModeKHR mode) {
			selected = mode;
			return super.presentation(mode);
		}
	}

	private PresentationModeSwapchainConfiguration configuration;
	private MockSwapchainBuilder builder;

	@BeforeEach
	void before() {
		builder = new MockSwapchainBuilder();
		configuration = new PresentationModeSwapchainConfiguration(List.of(VkPresentModeKHR.MAILBOX_KHR));
	}

	@Test
	void configure() {
		final var properties = new MockSurfaceProperties() {
			@Override
			public List<VkPresentModeKHR> modes() {
				return List.of(VkPresentModeKHR.MAILBOX_KHR);
			}
		};
		configuration.configure(builder, properties);
		assertEquals(VkPresentModeKHR.MAILBOX_KHR, builder.selected);
	}

	@Test
	void fallback() {
		configuration.configure(builder, new MockSurfaceProperties());
		assertEquals(VkPresentModeKHR.FIFO_KHR, builder.selected);
	}
}
