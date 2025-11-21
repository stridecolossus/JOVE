package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanException;
import org.sarge.jove.util.EnumMask;

class FenceTest {
	static class MockFenceLibrary extends MockVulkanLibrary {
		private boolean destroyed;
		private boolean reset;
		private boolean waiting;
		private VkResult status;

		@Override
		public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(VkFenceCreateFlag.SIGNALED), pCreateInfo.flags);
			assertEquals(null, pAllocator);
			pFence.set(MemorySegment.ofAddress(2));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyFence(LogicalDevice device, Fence fence, Handle pAllocator) {
			assertNotNull(device);
			assertNotNull(fence);
			assertEquals(null, pAllocator);
			destroyed = true;
		}

		@Override
		public VkResult vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences) {
			assertEquals(1, fenceCount);
			assertEquals(1, pFences.length);
			reset = true;
			return VkResult.SUCCESS;
		}

		@Override
		public int vkGetFenceStatus(LogicalDevice device, Fence fence) {
			assertNotNull(device);
			assertNotNull(fence);
			return status.value();
		}

		@Override
		public VkResult vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout) {
			assertEquals(1, fenceCount);
			assertEquals(1, pFences.length);
			assertEquals(true, waitAll);
			assertEquals(Long.MAX_VALUE, timeout);
			waiting = true;
			return VkResult.SUCCESS;
		}
	}

	private Fence fence;
	private LogicalDevice device;
	private MockFenceLibrary library;

	@BeforeEach
	void before() {
		library = new MockFenceLibrary();
		device = new MockLogicalDevice(library);
		fence = new Fence(new Handle(2), device);
	}

	@Test
	void create() {
		final Fence fence = Fence.create(device, VkFenceCreateFlag.SIGNALED);
		assertEquals(new Handle(2), fence.handle());
	}

	@Nested
	class StatusTest {
    	@Test
    	void signalled() {
    		library.status = VkResult.SUCCESS;
    		assertEquals(true, fence.signalled());
    	}

    	@Test
    	void waiting() {
    		library.status = VkResult.NOT_READY;
    		assertEquals(false, fence.signalled());
    	}

    	@Test
    	void invalid() {
    		library.status = VkResult.ERROR_DEVICE_LOST;
    		assertThrows(VulkanException.class, () -> fence.signalled());
    	}
	}

	@Test
	void reset() {
		fence.reset();
		assertEquals(true, library.reset);
	}

	@Test
	void waitReady() {
		fence.waitReady();
		assertEquals(true, library.waiting);
	}

	@Test
	void destroy() {
		fence.destroy();
		assertEquals(true, fence.isDestroyed());
		assertEquals(true, library.destroyed);
	}
}
