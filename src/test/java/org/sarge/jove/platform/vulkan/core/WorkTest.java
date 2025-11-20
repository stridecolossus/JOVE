package org.sarge.jove.platform.vulkan.core;
import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStage.FRAGMENT_SHADER;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class WorkTest {
	private static class MockQueueLibrary extends MockVulkanLibrary {
		private boolean submitted;
		private EnumMask<VkCommandBufferUsage> flags;

		@Override
		public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
			assertNotNull(queue);
			assertNotNull(fence);
			assertEquals(submitCount, pSubmits.length);
			for(var submit : pSubmits) {
				assertEquals(1, submit.commandBufferCount);
				assertEquals(1, submit.pCommandBuffers.length);
				assertEquals(submit.waitSemaphoreCount, submit.pWaitSemaphores.length);
				assertEquals(submit.signalSemaphoreCount, submit.pSignalSemaphores.length);
				if(submit.waitSemaphoreCount > 0) {
					assertArrayEquals(new int[]{128}, submit.pWaitDstStageMask);
				}
			}
			submitted = true;
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, Handle[] pCommandBuffers) {
			pCommandBuffers[0] = new Handle(3);
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
			this.flags = pBeginInfo.flags;
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
			pFence.set(new Handle(4));
			return VkResult.SUCCESS;
		}
	}

	private Pool pool;
	private Buffer buffer;
	private LogicalDevice device;
	private MockQueueLibrary library;

	@BeforeEach
	void before() {
		// Init device
		library = new MockQueueLibrary();
		device = new MockLogicalDevice(library);

		// Create work queue
		final Family family = new Family(0, 1, Set.of());
		final WorkQueue queue = new WorkQueue(new Handle(1), family);

		// Init command pool
		pool = new Pool(new Handle(2), device, queue);

		// Create a command buffer
		buffer = pool
				.allocate(1, true)
				.getFirst()
				.begin()
				.end();
	}

	@DisplayName("Work can be submitted to a queue")
	@Test
	void submit() {
		final Work work = new Work.Builder()
				.add(buffer)
				.wait(new MockVulkanSemaphore(), Set.of(FRAGMENT_SHADER))
				.signal(new MockVulkanSemaphore())
				.build();

		final Fence fence = new Fence(new Handle(2), device);
		work.submit(fence);
		assertEquals(true, library.submitted);
	}

	@DisplayName("All command buffers in a work submission must be ready for execution")
	@Test
	void ready() {
		final Buffer unready = pool.allocate(1, true).getFirst();
		assertThrows(IllegalStateException.class, () -> new Work.Builder().add(unready).build());
	}

	@DisplayName("All command buffers in a work submission must submit to the same queue family")
	@Test
	void family() {
		final WorkQueue queue = new WorkQueue(new Handle(4), new Family(1, 2, Set.of()));

		final Buffer other = new Pool(new Handle(5), device, queue)
				.allocate(1, true)
				.getFirst()
				.begin()
				.end();

		final var work = new Work.Builder()
				.add(buffer)
				.add(other);

		assertThrows(IllegalArgumentException.class, () -> work.build());
	}

	@DisplayName("A semaphore in a work submission cannot be used as both a wait and a signal")
	@Test
	void both() {
		final var semaphore = new MockVulkanSemaphore();

		final var work = new Work.Builder()
				.add(buffer)
				.wait(semaphore, Set.of(FRAGMENT_SHADER))
				.signal(semaphore);

		assertThrows(IllegalArgumentException.class, () -> work.build());
	}

	@DisplayName("A command can be submitted as a one-time task")
	@Test
	void once() {
		final Buffer once = Work.submit(new MockCommand(), pool);
		assertEquals(true, library.submitted);
		assertEquals(new EnumMask<>(VkCommandBufferUsage.ONE_TIME_SUBMIT), library.flags);
		assertEquals(true, once.isPrimary());
		assertEquals(pool, once.pool());
		// TODO - assertEquals(Stage.INVALID, once.stage());
	}
}
