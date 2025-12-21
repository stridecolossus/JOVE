package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.jove.util.Mockery.Mock;

class FenceTest {
	@SuppressWarnings("unused")
	private static class MockFenceLibrary extends MockLibrary {
		public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
			assertEquals(VkStructureType.FENCE_CREATE_INFO, pCreateInfo.sType);
			assertEquals(new EnumMask<>(VkFenceCreateFlags.SIGNALED), pCreateInfo.flags);
			init(pFence);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences) {
			assertEquals(fenceCount, pFences.length);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout) {
			assertEquals(fenceCount, pFences.length);
			assertEquals(true, waitAll);
			assertEquals(Long.MAX_VALUE, timeout);
			return VkResult.VK_SUCCESS;
		}
	}

	private Fence fence;
	private LogicalDevice device;
	private Mockery mockery;
	private Fence.Library library;

	@BeforeEach
	void before() {
		mockery = new Mockery(Fence.Library.class);
		mockery.implement(new MockFenceLibrary());
		library = mockery.proxy();
		device = new MockLogicalDevice(library);
		fence = Fence.create(device, VkFenceCreateFlags.SIGNALED); //new Fence(new Handle(2), device);
	}

	@Test
	void constructor() {
		assertTrue(fence.signalled());
	}

	@Nested
	class StatusTest {
		private Mock mock;

		@BeforeEach
		void before() {
			mock = mockery.mock("vkGetFenceStatus");
		}

    	@Test
    	void signalled() {
    		mock.result(VkResult.VK_SUCCESS.value());
    		assertTrue(fence.signalled());
    	}

    	@Test
    	void waiting() {
    		mock.result(VkResult.VK_NOT_READY.value());
    		assertFalse(fence.signalled());
    	}

    	@Test
    	void invalid() {
    		mock.result(VkResult.VK_ERROR_DEVICE_LOST.value());
    		assertThrows(VulkanException.class, () -> fence.signalled());
    	}
	}

	@Test
	void reset() {
		fence.reset();
		assertEquals(1, mockery.mock("vkResetFences").count());
	}

	@Test
	void waitReady() {
		fence.waitReady();
		assertEquals(1, mockery.mock("vkWaitForFences").count());
	}

	@Test
	void destroy() {
		fence.destroy();
		assertTrue(fence.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyFence").count());
	}
}
