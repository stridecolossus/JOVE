package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;

class VulkanSemaphoreTest {
	@SuppressWarnings("unused")
	private static class MockSemaphoreLibrary extends MockLibrary {
		public VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
			assertEquals(VkStructureType.SEMAPHORE_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			init(pSemaphore);
			return VkResult.VK_SUCCESS;
		}
	}

	private VulkanSemaphore semaphore;
	private LogicalDevice device;
	private VulkanSemaphore.Library library;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(VulkanSemaphore.Library.class);
		mockery.implement(new MockSemaphoreLibrary());
		library = mockery.proxy();
		device = new MockLogicalDevice(library);
		semaphore = VulkanSemaphore.create(device);
	}

	@Test
	void create() {
		assertNotNull(semaphore.handle());
		assertFalse(semaphore.isDestroyed());
	}

	@Test
	void destroy() {
		semaphore.destroy();
		assertTrue(semaphore.isDestroyed());
		assertTrue(mockery.mock("vkDestroySemaphore").isInvoked());
	}
}
