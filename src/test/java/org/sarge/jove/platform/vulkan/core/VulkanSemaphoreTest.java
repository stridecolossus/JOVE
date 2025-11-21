package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;

class VulkanSemaphoreTest {
	private static class MockSemaphoreLibrary extends MockVulkanLibrary {
		private boolean destroyed;

		@Override
		public VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
			assertNotNull(device);
			assertNotNull(pCreateInfo);
			assertEquals(null, pAllocator);
			pSemaphore.set(MemorySegment.ofAddress(2));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroySemaphore(LogicalDevice device, VulkanSemaphore semaphore, Handle pAllocator) {
			assertNotNull(device);
			assertNotNull(semaphore);
			assertEquals(null, pAllocator);
			destroyed = true;
		}
	}

	private VulkanSemaphore semaphore;
	private LogicalDevice device;
	private MockSemaphoreLibrary library;

	@BeforeEach
	void before() {
		library = new MockSemaphoreLibrary();
		device = new MockLogicalDevice(library);
		semaphore = VulkanSemaphore.create(device);
	}

	@Test
	void destroy() {
		semaphore.destroy();
		assertEquals(true, semaphore.isDestroyed());
		assertEquals(true, library.destroyed);
	}
}
