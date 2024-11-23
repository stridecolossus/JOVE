package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

class DefaultVulkanFrameTest {
	private DefaultVulkanFrame frame;
	private Swapchain swapchain;
	private VulkanSemaphore available, ready;
	private Fence fence;

	@BeforeEach
	void before() {
		swapchain = mock(Swapchain.class);
		available = mock(VulkanSemaphore.class);
		ready = mock(VulkanSemaphore.class);
		fence = mock(Fence.class);
		frame = new DefaultVulkanFrame(available, ready, fence);
		when(swapchain.acquire(available, null)).thenReturn(2);
	}

	@Test
	void acquire() {
		assertEquals(2, frame.acquire(swapchain));
		verify(fence).waitReady();
		verify(fence).reset();
	}

	@Test
	void invalidated() {
		when(swapchain.acquire(available, null)).thenThrow(SwapchainInvalidated.class);
		assertThrows(SwapchainInvalidated.class, () -> frame.acquire(swapchain));
		verify(fence).waitReady();
		verify(fence, never()).reset();
		verifyNoMoreInteractions(fence);
	}

	@Test
	void present() {
		final var buffer = new MockCommandBuffer();
		buffer.begin().end();
		final WorkQueue queue = buffer.pool().queue();
		frame.present(buffer, 2, swapchain);
		// TODO - how to check submission?
		verify(fence).waitReady();
		verify(swapchain).present(queue, 2, ready);
	}

	@Test
	void destroy() {
		frame.destroy();
		verify(available).destroy();
		verify(ready).destroy();
		verify(fence).destroy();
	}
}
