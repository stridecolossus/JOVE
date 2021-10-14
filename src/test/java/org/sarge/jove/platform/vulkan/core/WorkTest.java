package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.Command.Pool;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class WorkTest extends AbstractVulkanTest {
	private Queue queue;
	private Pool pool;
	private Buffer buffer;

	@BeforeEach
	void before() {
		// Create queue
		final Queue.Family family = new Queue.Family(0, 1, Set.of());
		queue = new Queue(new Handle(1), family);

		// Create pool
		pool = mock(Command.Pool.class);
		when(pool.queue()).thenReturn(queue);
		when(pool.device()).thenReturn(dev);

		// Create command buffer
		buffer = mock(Command.Buffer.class);
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(2));
		when(buffer.isReady()).thenReturn(true);
	}

	@Test
	void constructor() {
		final Work work = Work.of(buffer);
		assertNotNull(work);
		work.submit(null);
	}

	@Nested
	class BuilderTests {
		private Work.Builder builder;
		private Semaphore semaphore;

		@BeforeEach
		void before() {
			builder = new Work.Builder(pool);
			semaphore = mock(Semaphore.class);
			when(semaphore.handle()).thenReturn(new Handle(3));
		}

		@Test
		void buildEmptyBuffers() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void addNotRecorded() {
			when(buffer.isReady()).thenReturn(false);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void addDifferentQueueFamily() {
			final Queue other = new Queue(new Handle(999), new Family(1, 2, Set.of()));
			when(buffer.pool()).thenReturn(mock(Pool.class));
			when(buffer.pool().queue()).thenReturn(other);
			assertThrows(IllegalArgumentException.class, () -> builder.add(buffer));
		}

		@Test
		void waitEmptyPipelineStages() {
			assertThrows(IllegalArgumentException.class, () -> builder.wait(semaphore, Set.of()));
		}

		@Test
		void build() {
			// Construct work submission
			final Work work = builder
					.add(buffer)
					.wait(semaphore, Set.of(VkPipelineStage.TOP_OF_PIPE))
					.signal(semaphore)
					.build();
			assertNotNull(work);

			// Submit work
			final Fence fence = mock(Fence.class);
			work.submit(fence);

			// Check API
			final ArgumentCaptor<VkSubmitInfo[]> captor = ArgumentCaptor.forClass(VkSubmitInfo[].class);
			verify(lib).vkQueueSubmit(eq(queue), eq(1), captor.capture(), eq(fence));

			// Extract submission descriptor
			final VkSubmitInfo[] array = captor.getValue();
			assertNotNull(array);
			assertEquals(1, array.length);

			// Check submission descriptor
			final VkSubmitInfo info = array[0];
			assertNotNull(info);
			assertEquals(1, info.commandBufferCount);
			assertEquals(1, info.waitSemaphoreCount);
			assertEquals(1, info.signalSemaphoreCount);
			assertNotNull(info.pCommandBuffers);
			assertNotNull(info.pWaitSemaphores);
			assertNotNull(info.pWaitDstStageMask);
			assertNotNull(info.pSignalSemaphores);
		}
	}
}
