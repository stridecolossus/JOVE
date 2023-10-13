package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure;

public class FenceTest {
	private Fence fence;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();
		fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
	}

	private void status(VkResult result) {
		Mockito.when(lib.vkGetFenceStatus(dev, fence)).thenReturn(result);
	}

	@Test
	void create() {
		final var expected = new VkFenceCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				return dataEquals((Structure) obj);
			}
		};
		expected.flags = BitMask.of(VkFenceCreateFlag.SIGNALED);
		verify(lib).vkCreateFence(dev, expected, null, dev.factory().pointer());
	}

	@DisplayName("A signalled fence...")
	@Nested
	class Signalled {
		@DisplayName("can test whether it is signalled")
		@Test
		void signalled() {
			status(VkResult.SUCCESS);
			assertEquals(true, fence.signalled());
		}

		@DisplayName("can be reset")
		@Test
		void reset() {
			fence.reset();
			verify(lib).vkResetFences(dev, 1, NativeObject.array(List.of(fence)));
		}
	}

	@DisplayName("An unsignalled fence...")
	@Nested
	class Unsignalled {
		@DisplayName("can test whether it is signalled")
		@Test
		void signalled() {
			status(VkResult.NOT_READY);
			assertEquals(false, fence.signalled());
		}

		@DisplayName("can block until it becomes signalled")
		@Test
		void waitReady() {
			fence.waitReady();
			verify(lib).vkWaitForFences(dev, 1, NativeObject.array(List.of(fence)), true, Long.MAX_VALUE);
		}
	}

	@DisplayName("An invalid fence cannot be tested for whether it is signalled")
	@Test
	void error() {
		status(VkResult.ERROR_DEVICE_LOST);
		assertThrows(VulkanException.class, () -> fence.signalled());
	}

	@DisplayName("A fence can be destroyed")
	@Test
	void destroy() {
		fence.destroy();
		verify(lib).vkDestroyFence(dev, fence, null);
	}
}
