package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;

class VulkanFrameTest {
	private VulkanFrame frame;
	private LogicalDevice device;
	private Swapchain swapchain;
	private boolean presented;
	private MockVulkanSemaphore available, ready;
	private Command.Buffer sequence;
	private MockFence fence;

	@BeforeEach
	void before() {
		device = new MockLogicalDevice();
		swapchain = new MockSwapchain(device) {
			@Override
			public int acquire(VulkanSemaphore semaphore, Fence fence) throws SwapchainInvalidated {
				assertEquals(available, semaphore);
				//assertEquals
				return 0;
			}

			@Override
			public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws SwapchainInvalidated {
				assertEquals(0, index);
				assertEquals(ready, semaphore);
				presented = true;
			}
		};
		presented = false;

		final var pool = new MockCommandPool(device);
		sequence = pool.allocate(1, true).getFirst();

		available = new MockVulkanSemaphore();
		ready = new MockVulkanSemaphore();
		fence = new MockFence();
		frame = new VulkanFrame(available, ready, fence);
	}

	@Test
	void acquire() {
		assertEquals(0, frame.acquire(swapchain));
		assertEquals(1, fence.wait);
		assertEquals(1, fence.reset);
	}

	@Test
	void render() {
		frame.render(sequence);
		assertEquals(1, fence.wait);
	}

	@Test
	void present() {
		frame.present(sequence, 0, swapchain);
		assertEquals(true, presented);
	}
}
