package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;

class SurfaceFormatSwapchainConfigurationTest {
	private static class MockSwapchainBuilder extends Swapchain.Builder {
		VkSurfaceFormatKHR selected;

		@Override
		public Builder format(VkSurfaceFormatKHR format) {
			this.selected = format;
			return super.format(format);
		}
	}

	private MockSwapchainBuilder builder;
	private Properties properties;
	private VkSurfaceFormatKHR format;

	@BeforeEach
	void before() {
		format = new SurfaceFormatWrapper(VkFormat.B8G8R8A8_UNORM, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);
		builder = new MockSwapchainBuilder();
		properties = new MockSurfaceProperties() {
			@Override
			public List<VkSurfaceFormatKHR> formats() {
				return List.of(format);
			}
		};
	}

	@Test
	void preferred() {
		final var configuration = new SurfaceFormatSwapchainConfiguration(new SurfaceFormatWrapper(format));
		configuration.configure(builder, properties);
		assertEquals(format, builder.selected);
	}

	@Test
	void fallback() {
		final var unavailable = new SurfaceFormatWrapper(VkFormat.UNDEFINED, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);
		final var configuration = new SurfaceFormatSwapchainConfiguration(unavailable);
		configuration.configure(builder, properties);
		assertEquals(format, builder.selected);
	}
}
