package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStageFlags.FRAGMENT_SHADER;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateFlags;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.core.CommandTest.MockCommandLibrary;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.Mockery;

class WorkTest {
	private Pool pool;
	private Buffer buffer;
	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
//		// Init library
//		final var library = new MockCommandLibrary() {
//			@Override
//			public VkResult vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
//				super.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
//				return VkResult.VK_SUCCESS;
//			}
//
//			@Override
//			public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
//				super.vkQueueSubmit(queue, submitCount, pSubmits, fence);
////				submit = pSubmits[0];
//				return VkResult.VK_SUCCESS;
//			}
//		};

		mockery = new Mockery(new MockCommandLibrary(), Command.Library.class);
		device = new MockLogicalDevice(mockery.proxy());

		// Create work queue
		final Family family = new Family(0, 1, Set.of());
		final WorkQueue queue = new WorkQueue(new Handle(1), family);

		// Init command pool
		pool = Pool.create(device, queue, VkCommandPoolCreateFlags.RESET_COMMAND_BUFFER);

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

		final Fence fence = new MockFence();
		work.submit(fence);

		// TODO
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

		assertThrows(IllegalStateException.class, () -> work.build());
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

	@Disabled
	@DisplayName("A command can be submitted as a one-time task")
	@Test
	void once() {

//		new Work.Builder()
//				.add(buffer)
//				.build()
//				.submit(new MockFence());

		final Buffer once = Work.submit(new MockCommand(), pool);
		//assertEquals(new EnumMask<>(VkCommandBufferUsageFlags.ONE_TIME_SUBMIT), submit.
		assertEquals(true, once.isPrimary());
		assertEquals(pool, once.pool());
		// TODO - assertEquals(Stage.INVALID, once.stage());
	}
}
