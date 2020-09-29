package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

import com.sun.jna.Pointer;

public class FenceTest extends AbstractVulkanTest {
	private Fence fence;

	@Nested
	class FenceTests {
		private IntSupplier status;

		@BeforeEach
		public void before() {
			status = mock(IntSupplier.class);
			fence = new Fence(mock(Pointer.class), device, status);
		}

		@Test
		public void signaled() {
			when(status.getAsInt()).thenReturn(VkResult.VK_SUCCESS.value());
			assertEquals(Fence.Status.SIGNALED, fence.status());
		}

		@Test
		public void notReady() {
			when(status.getAsInt()).thenReturn(VkResult.VK_NOT_READY.value());
			assertEquals(Fence.Status.NOT_READY, fence.status());
		}

		@Test
		public void lost() {
			when(status.getAsInt()).thenReturn(VkResult.VK_ERROR_DEVICE_LOST.value());
			assertEquals(Fence.Status.LOST, fence.status());
		}
	}

	@Nested
	class CreateTests {
		@BeforeEach
		public void create() {
			fence = Fence.create(device, true);
		}

		@Test
		public void status() {
			when(library.vkGetFenceStatus(device.handle(), fence.handle())).thenReturn(VkResult.VK_NOT_READY.value());
			assertEquals(Fence.Status.NOT_READY, fence.status());
		}

		@Test
		public void destroy() {
			final Pointer handle = fence.handle();
			fence.destroy();
			verify(library).vkDestroyFence(device.handle(), handle, null);
		}
	}

	@Nested
	class GroupTests {
		private Fence.Group group;

		@BeforeEach
		public void before() {
			fence = new Fence(mock(Pointer.class), device, () -> 42);
			group = new Fence.Group(device, Arrays.asList(fence));
		}

		@Test
		public void reset() {
			group.reset();
			verify(library).vkResetFences(device.handle(), 1, new Pointer[]{fence.handle()});
		}

		@Test
		public void waitAll() {
			group.wait(true);
			verify(library).vkWaitForFences(device.handle(), 1, new Pointer[]{fence.handle()}, VulkanBoolean.TRUE, Long.MAX_VALUE);
		}

		@Test
		public void waitAny() {
			group.wait(false);
			verify(library).vkWaitForFences(device.handle(), 1, new Pointer[]{fence.handle()}, VulkanBoolean.FALSE, Long.MAX_VALUE);
		}
	}
}
