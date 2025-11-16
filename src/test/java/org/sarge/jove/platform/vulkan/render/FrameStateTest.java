package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class FrameStateTest {
	private FrameState frame;
	private MockSwapchain swapchain;
	private MockFence fence;
	private Buffer sequence;
	private boolean presented;

	@BeforeEach
	void before() {
		// Init synchronisation primitives
		final var available = new MockVulkanSemaphore();
		final var ready = new MockVulkanSemaphore();
		fence = new MockFence();

		// Init command sequence
		sequence = new MockCommandBuffer();

		// Create swapchain
		swapchain = new MockSwapchain(new MockLogicalDevice()) {
			@Override
			public int acquire(VulkanSemaphore semaphore, Fence fence) throws Invalidated {
				assertEquals(available, semaphore);
				return 3;
			}

			@Override
			public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws Invalidated {
				assertEquals(sequence.pool().queue(), queue);
				assertEquals(ready, semaphore);
				presented = true;
			}
		};

		// Create in-flight frame
		presented = false;
		frame = new FrameState(available, ready, fence);
	}

	@Test
	void acquire() {
		assertEquals(3, frame.acquire(swapchain));
		assertEquals(1, fence.wait);
		assertEquals(1, fence.reset);
	}

	@Test
	void render() {
		frame.render(sequence);
		assertEquals(1, fence.wait);
		// TODO - check submitted to pool with fence?
	}

	@Test
	void present() {
		frame.present(sequence, 0, swapchain);
		assertEquals(true, presented);
	}

	@Test
	void invalid() {
		final var semaphore = new MockVulkanSemaphore();
		assertThrows(IllegalArgumentException.class, () -> new FrameState(semaphore, semaphore, fence));
	}
}
