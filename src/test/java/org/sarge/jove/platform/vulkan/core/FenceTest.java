package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class FenceTest extends AbstractVulkanTest {
	private Fence fence;

	@BeforeEach
	void before() {
		fence = new Fence(new Pointer(1), dev);
	}

	@Test
	void signalled() {
		when(lib.vkGetFenceStatus(dev, fence)).thenReturn(VkResult.SUCCESS.value());
		assertEquals(true, fence.signalled());
		verify(lib).vkGetFenceStatus(dev, fence);
	}

	@Test
	void notSignalled() {
		when(lib.vkGetFenceStatus(dev, fence)).thenReturn(VkResult.NOT_READY.value());
		assertEquals(false, fence.signalled());
	}

	@Test
	void signalledError() {
		when(lib.vkGetFenceStatus(dev, fence)).thenReturn(VkResult.ERROR_DEVICE_LOST.value());
		assertThrows(VulkanException.class, () -> fence.signalled());
	}

	@Test
	void reset() {
		fence.reset();
		verify(lib).vkResetFences(dev, 1, NativeObject.toArray(List.of(fence)));
	}

	@Test
	void waitReady() {
		fence.waitReady();
		verify(lib).vkWaitForFences(dev, 1, NativeObject.toArray(List.of(fence)), VulkanBoolean.TRUE, Long.MAX_VALUE);
	}

	@Test
	void destroy() {
		fence.destroy();
		verify(lib).vkDestroyFence(dev, fence, null);
	}

	@Test
	void create() {
		// Mock API
		final ArgumentCaptor<VkFenceCreateInfo> captor = ArgumentCaptor.forClass(VkFenceCreateInfo.class);
		when(lib.vkCreateFence(eq(dev), captor.capture(), isNull(), isA(PointerByReference.class))).thenReturn(VulkanLibrary.SUCCESS);

		// Create fence
		fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		assertNotNull(fence);

		// Check descriptor
		final VkFenceCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(VkFenceCreateFlag.SIGNALED.value(), info.flags);
	}
}
