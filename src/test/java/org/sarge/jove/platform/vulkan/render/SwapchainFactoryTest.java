package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.SwapchainTest.MockSwapchainLibrary;

class SwapchainFactoryTest {
	private SwapchainFactory factory;

	@BeforeEach
	void before() {
		final var library = new MockSwapchainLibrary();
		final var device = new MockLogicalDevice(library);
		final var surface = new MockVulkanSurface(library);
		final var physical = new MockPhysicalDevice(library);
		final var properties = surface.new PropertiesAdapter(physical);
		factory = new SwapchainFactory(device, properties);
	}

	@Test
	void swapchain() {
		final Swapchain swapchain = factory.swapchain();
		assertEquals(false, swapchain.isDestroyed());
	}

	@Test
	void recreate() {
		// TODO - fiddle one of the properties
		final Swapchain previous = factory.swapchain();
		factory.recreate();
		final Swapchain swapchain = factory.swapchain();
		assertEquals(false, swapchain.isDestroyed());
		assertNotSame(previous, swapchain);
	}

	@Test
	void destroy() {
		final Swapchain swapchain = factory.swapchain();
		factory.destroy();
		assertEquals(true, swapchain.isDestroyed());
	}
}
