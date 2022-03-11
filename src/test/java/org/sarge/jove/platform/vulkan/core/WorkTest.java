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
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Work.Batch;
import org.sarge.jove.platform.vulkan.core.Work.DefaultWork;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerArray;

public class WorkTest extends AbstractVulkanTest {
	private Queue queue;
	private Pool pool;
	private Buffer buffer;
	private DefaultWork work;

	@BeforeEach
	void before() {
		// Create queue
		final Queue.Family family = new Family(0, 1, Set.of());
		queue = new Queue(new Handle(1), family);

		// Create pool
		pool = mock(Pool.class);
		when(pool.queue()).thenReturn(queue);
		when(pool.device()).thenReturn(dev);

		// Create command buffer
		buffer = mock(Buffer.class);
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(2));
		when(buffer.isReady()).thenReturn(true);

		// Create work instance
		work = Work.of(buffer);
	}

	@Test
	void constructor() {
		assertNotNull(work);
	}

	@Test
	void submit() {
		// Submit work
		final Fence fence = mock(Fence.class);
		work.submit(fence);

		// Init expected submission descriptor
		final VkSubmitInfo expected = new VkSubmitInfo() {
			@Override
			public boolean equals(Object obj) {
				final VkSubmitInfo info = (VkSubmitInfo) obj;
				assertEquals(1, info.commandBufferCount);
				assertEquals(NativeObject.array(List.of(buffer)), info.pCommandBuffers);
				return true;
			}
		};

		// Check API
		verify(lib).vkQueueSubmit(queue, 1, new VkSubmitInfo[]{expected}, fence);
	}

	@Test
	void equals() {
		assertEquals(true, work.equals(work));
		assertEquals(true, work.equals(Work.of(buffer)));
		assertEquals(false, work.equals(null));
		assertEquals(false, work.equals(mock(Work.class)));
	}

	@Nested
	class BatchTest {
		private Batch batch;

		@BeforeEach
		void before() {
			batch = new Batch(List.of(work, work));
		}

		@Test
		void submit() {
			batch.submit(null);
		}

		@Test
		void invalidQueueFamily() {
			final Command.Pool other = mock(Pool.class);
			final DefaultWork invalid = mock(DefaultWork.class);
			when(invalid.pool()).thenReturn(other);
			when(other.queue()).thenReturn(new Queue(new Handle(999), new Family(1, 2, Set.of())));
			assertThrows(IllegalArgumentException.class, () -> new Batch(List.of(work, invalid)));
		}

		@Test
		void equals() {
			assertEquals(true, batch.equals(batch));
			assertEquals(true, batch.equals(new Batch(List.of(work, work))));
			assertEquals(false, batch.equals(null));
			assertEquals(false, batch.equals(mock(Batch.class)));
		}
	}

	@Nested
	class BuilderTest {
		private Work.Builder builder;
		private Semaphore wait, signal;

		@BeforeEach
		void before() {
			wait = mock(Semaphore.class);
			signal = mock(Semaphore.class);
			builder = new Work.Builder(pool);
		}

		@Test
		void add() {
			builder.add(buffer);
		}

		@Test
		void addNotRecorded() {
			when(buffer.isReady()).thenReturn(false);
			assertThrows(IllegalStateException.class, () -> builder.add(buffer));
		}

		@Test
		void addInvalidQueueFamily() {
			final Pool other = mock(Pool.class);
			when(buffer.pool()).thenReturn(other);
			when(other.queue()).thenReturn(new Queue(new Handle(999), new Family(1, 2, Set.of())));
			assertThrows(IllegalArgumentException.class, () -> builder.add(buffer));
		}

		@Test
		void waitSemaphore() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
		}

		@Test
		void waitSemaphoreDuplicate() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
			assertThrows(IllegalArgumentException.class, () -> builder.wait(wait, VkPipelineStage.TOP_OF_PIPE));
		}

		@Test
		void signalSemaphore() {
			builder.signal(signal);
		}

		@Test
		void invalidSemaphores() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
			builder.signal(wait);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void build() {
			// Init semaphores
			final Handle handle = new Handle(3);
			when(wait.handle()).thenReturn(handle);
			when(signal.handle()).thenReturn(handle);

			// Build work
			work = builder
					.add(buffer)
					.add(buffer)
					.wait(wait, VkPipelineStage.TOP_OF_PIPE)
					.signal(signal)
					.build();

			// Submit work
			assertNotNull(work);
			work.submit(null);

			// Init expected submission descriptor
			final VkSubmitInfo expected = new VkSubmitInfo() {
				@Override
				public boolean equals(Object obj) {
					final VkSubmitInfo info = (VkSubmitInfo) obj;
					assertEquals(2, info.commandBufferCount);
					assertEquals(NativeObject.array(List.of(buffer, buffer)), info.pCommandBuffers);

					// Check wait semaphores
					assertEquals(1, info.waitSemaphoreCount);
					assertEquals(NativeObject.array(Set.of(wait)), info.pWaitSemaphores);
					assertEquals(new IntegerArray(new int[]{VkPipelineStage.TOP_OF_PIPE.value()}), info.pWaitDstStageMask);

					// Check signal semaphores
					assertEquals(1, info.signalSemaphoreCount);
					assertEquals(NativeObject.array(Set.of(signal)), info.pSignalSemaphores);

					return true;
				}
			};

			// Check API
			verify(lib).vkQueueSubmit(queue, 1, new VkSubmitInfo[]{expected}, null);
		}
	}
}
