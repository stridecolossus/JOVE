package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.MockLibraryFactory;

class VulkanSemaphoreTest {
	private MockLibraryFactory factory;
	private VulkanLibrary lib;
	private MockLogicalDevice device;

	@BeforeEach
	void before() {
		factory = new MockLibraryFactory(VulkanLibrary.class);
		lib = factory.proxy();
		device = new MockLogicalDevice(lib);
	}

	@Test
	void create() {
		final VulkanSemaphore semaphore = VulkanSemaphore.create(device);
		semaphore.destroy();
		assertEquals(1, factory.get("vkCreateSemaphore").count());
		assertEquals(1, factory.get("vkDestroySemaphore").count());
	}
}
