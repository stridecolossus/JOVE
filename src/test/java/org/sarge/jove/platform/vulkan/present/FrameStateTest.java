package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class FrameStateTest {
	private FrameState frame;
	private MockSwapchain swapchain;
	private MockVulkanSemaphore available, ready;
	private MockFence fence;
	private Buffer sequence;
	private boolean presented;

	@BeforeEach
	void before() {
		// Init synchronisation primitives
		available = new MockVulkanSemaphore();
		ready = new MockVulkanSemaphore();
		fence = new MockFence();

		// Init command sequence
		sequence = new MockCommandBuffer();
		sequence.begin();
		sequence.end();

		// Create swapchain
		swapchain = new MockSwapchain() {
			@Override
			public int acquire(VulkanSemaphore semaphore, Fence fence) throws Invalidated {
				assertEquals(available, semaphore);
				return 3;
			}

			@Override
			public void present(WorkQueue queue, int index, Set<VulkanSemaphore> semaphores) throws Invalidated {
				assertEquals(3, index);
				assertEquals(sequence.pool().queue(), queue);
				assertEquals(Set.of(ready), semaphores);
				presented = true;
			}
		};

		// Create in-flight frame
		presented = false;
		frame = new FrameState(1, available, ready, fence);
	}

	@Test
	void index() {
		assertEquals(1, frame.index());
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
		frame.present(sequence, 3, swapchain);
		assertEquals(true, presented);
	}

	@Test
	void invalid() {
		final var semaphore = new MockVulkanSemaphore();
		assertThrows(IllegalArgumentException.class, () -> new FrameState(1, semaphore, semaphore, fence));
	}

	@Test
	void destroy() {
		frame.destroy();
		assertEquals(true, available.isDestroyed());
		assertEquals(true, ready.isDestroyed());
		assertEquals(true, fence.isDestroyed());
	}
}
