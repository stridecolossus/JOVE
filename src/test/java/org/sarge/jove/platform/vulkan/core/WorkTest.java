package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Work.Batch;
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
	void of() {
		final Work work = Work.of(buffer);
		assertNotNull(work);
	}

	@Nested
	class BatchTests {
		private Semaphore semaphore() {
			final Semaphore semaphore = mock(Semaphore.class);
			when(semaphore.handle()).thenReturn(new Handle(3));
			return semaphore;
		}

		@Test
		void submit() {
			// Create work
			final Work work = new Work.Builder(pool)
					.add(buffer)
					.wait(semaphore(), Set.of(VkPipelineStage.TOP_OF_PIPE))
					.signal(semaphore())
					.build();

			// Create batch
			final Batch batch = Batch.of(List.of(work));
			assertNotNull(batch);

			// Submit work
			final Fence fence = mock(Fence.class);
			batch.submit(fence);

			// Check API
			final VkSubmitInfo expected = new VkSubmitInfo() {
				@Override
				public boolean equals(Object obj) {
					final VkSubmitInfo info = (VkSubmitInfo) obj;
					assertNotNull(info);
					assertEquals(1, info.commandBufferCount);
					assertEquals(1, info.waitSemaphoreCount);
					assertEquals(1, info.signalSemaphoreCount);
					assertNotNull(info.pCommandBuffers);
					assertNotNull(info.pWaitSemaphores);
					assertNotNull(info.pWaitDstStageMask);
					assertNotNull(info.pSignalSemaphores);
					return true;
				}
			};
			verify(lib).vkQueueSubmit(queue, 1, new VkSubmitInfo[]{expected}, fence);
		}
	}

	@Nested
	class BuilderTests {
		private Work.Builder builder;

		@BeforeEach
		void before() {
			builder = new Work.Builder(pool);
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
			assertThrows(IllegalArgumentException.class, () -> builder.wait(mock(Semaphore.class), Set.of()));
		}

		@Test
		void build() {
			final Work work = builder
					.add(buffer)
					.wait(mock(Semaphore.class), Set.of(VkPipelineStage.TOP_OF_PIPE))
					.signal(mock(Semaphore.class))
					.build();

			assertNotNull(work);
		}
	}
}
