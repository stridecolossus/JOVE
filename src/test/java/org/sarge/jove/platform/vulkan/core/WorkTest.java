package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerArray;

import com.sun.jna.Structure;

public class WorkTest extends AbstractVulkanTest {
	private Queue queue;
	private Pool pool;
	private Buffer buffer;
	private Work work;

	@BeforeEach
	void before() {
		// Create queue
		final Family family = new Family(0, 1, Set.of());
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
		assertEquals(pool, work.pool());
	}

	@DisplayName("Work can be submitted to a queue")
	@Test
	void submit() {
		// Submit work
		final Fence fence = mock(Fence.class);
		work.submit(fence);

		// Init expected submission descriptor
		final VkSubmitInfo expected = new VkSubmitInfo() {
			@Override
			public boolean equals(Object obj) {
				return dataEquals((Structure) obj);
			}
		};
		expected.commandBufferCount = 1;
		expected.pCommandBuffers = NativeObject.array(List.of(buffer));

		// Check API
		verify(lib).vkQueueSubmit(queue, 1, new VkSubmitInfo[]{expected}, fence);
	}

	@DisplayName("A work batch must all submit to the same queue family")
	@Test
	void invalid() {
		// Create a different pool
		final Pool other = mock(Pool.class);
		when(other.queue()).thenReturn(new Queue(new Handle(999), new Family(1, 2, Set.of())));

		// Create a buffer using this pool
		final Buffer buffer = mock(Buffer.class);
		when(buffer.pool()).thenReturn(other);
		when(buffer.isReady()).thenReturn(true);

		// Create work
		final Work invalid = Work.of(buffer);

		// Check cannot submit to different queues
		assertThrows(IllegalArgumentException.class, () -> Work.submit(List.of(work, invalid), null));
	}

	@Test
	void equals() {
		assertEquals(true, work.equals(work));
		assertEquals(true, work.equals(Work.of(buffer)));
		assertEquals(false, work.equals(null));
		assertEquals(false, work.equals(mock(Work.class)));
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

		@DisplayName("A command buffer can be added to the work")
		@Test
		void add() {
			builder.add(buffer);
		}

		@DisplayName("A command buffer that has not been recorded cannot be added to the work")
		@Test
		void addNotRecorded() {
			when(buffer.isReady()).thenReturn(false);
			assertThrows(IllegalStateException.class, () -> builder.add(buffer));
		}

		@DisplayName("All command buffers in the work must submit to the same queue family")
		@Test
		void addInvalidQueueFamily() {
			final Pool other = mock(Pool.class);
			when(buffer.pool()).thenReturn(other);
			when(other.queue()).thenReturn(new Queue(new Handle(999), new Family(1, 2, Set.of())));
			assertThrows(IllegalArgumentException.class, () -> builder.add(buffer));
		}

		@DisplayName("The work can be configured to wait for a semaphore")
		@Test
		void waitSemaphore() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
		}

		@DisplayName("A wait semaphore cannot be added to the work more than once")
		@Test
		void waitSemaphoreDuplicate() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
			assertThrows(IllegalArgumentException.class, () -> builder.wait(wait, VkPipelineStage.TOP_OF_PIPE));
		}

		@DisplayName("The work can be configured to signal a semaphore on completion")
		@Test
		void signalSemaphore() {
			builder.signal(signal);
		}

		@DisplayName("A semaphore cannot be used as both a wait and a signal")
		@Test
		void invalidSemaphores() {
			builder.wait(wait, VkPipelineStage.TOP_OF_PIPE);
			builder.signal(wait);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("A work submission can be constructed via the builder")
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
