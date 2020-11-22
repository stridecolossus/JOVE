package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class FenceTest extends AbstractVulkanTest {
	private Fence fence;
	private Pointer handle;

	@BeforeEach
	void before() {
		handle = new Pointer(1);
		fence = new Fence(handle, dev);
	}

	@Test
	void signalled() {
		when(lib.vkGetFenceStatus(dev.handle(), fence.handle())).thenReturn(VkResult.VK_SUCCESS.value());
		assertEquals(true, fence.signalled());
		verify(lib).vkGetFenceStatus(dev.handle(), fence.handle());
	}

	@Test
	void notSignalled() {
		when(lib.vkGetFenceStatus(dev.handle(), fence.handle())).thenReturn(VkResult.VK_NOT_READY.value());
		assertEquals(false, fence.signalled());
	}

	@Test
	void signalledError() {
		when(lib.vkGetFenceStatus(dev.handle(), fence.handle())).thenReturn(VkResult.VK_ERROR_DEVICE_LOST.value());
		assertThrows(VulkanException.class, () -> fence.signalled());
	}

	@Test
	void reset() {
		fence.reset();
		verify(lib).vkResetFences(dev.handle(), 1, new Pointer[]{handle});
	}

	@Test
	void waitReady() {
		fence.waitReady();
		verify(lib).vkWaitForFences(dev.handle(), 1, new Pointer[]{handle}, VulkanBoolean.TRUE, Long.MAX_VALUE);
	}

	@Test
	void destroy() {
		fence.destroy();
		verify(lib).vkDestroyFence(dev.handle(), fence.handle(), null);
	}

	@Test
	void create() {
		// Create fence
		fence = Fence.create(dev, VkFenceCreateFlag.VK_FENCE_CREATE_SIGNALED_BIT);
		assertNotNull(fence);

		// Check API
		final ArgumentCaptor<VkFenceCreateInfo> captor = ArgumentCaptor.forClass(VkFenceCreateInfo.class);
		verify(lib).vkCreateFence(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final VkFenceCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(VkFenceCreateFlag.VK_FENCE_CREATE_SIGNALED_BIT.value(), info.flags);
	}
}
