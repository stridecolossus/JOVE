package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.PointerToIntArray;

import com.sun.jna.Structure;

public class WorkTest {
	private WorkQueue queue;
	private CommandPool pool;
	private CommandBuffer buffer;
	private Work work;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();
		lib = dev.library();

		// Create work queue
		final Family family = new Family(2, 3, Set.of());
		queue = new WorkQueue(new Handle(1), family);

		// Create command buffer
		pool = new Command.CommandPool(new Handle(2), dev, queue);
		buffer = pool.primary().begin().end();

		// Create work instance
		work = new Work.Builder().add(buffer).build();
	}

	@Test
	void constructor() {
		assertEquals(pool, work.pool());
	}

	@DisplayName("Work can be submitted to a queue")
	@Test
	void submit() {
		// Submit work
		final Fence fence = Fence.create(dev);
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
		final PrimaryBuffer other = new MockCommandBuffer();
		other.begin().end();
		final Work invalid = Work.of(other);
		assertThrows(IllegalArgumentException.class, () -> Work.submit(List.of(work, invalid), null));
	}

	@Test
	void equals() {
		assertEquals(true, work.equals(work));
		assertEquals(false, work.equals(null));
	}

	@Nested
	class BuilderTest {
		private Work.Builder builder;
		private VulkanSemaphore wait, signal;

		@BeforeEach
		void before() {
			wait = mock(VulkanSemaphore.class);
			signal = mock(VulkanSemaphore.class);
			builder = new Work.Builder();
		}

		@DisplayName("A command buffer can be added to the work")
		@Test
		void add() {
			builder.add(buffer);
		}

		@DisplayName("A command buffer that has not been recorded cannot be added to the work")
		@Test
		void addNotRecorded() {
			buffer.reset();
			assertThrows(IllegalStateException.class, () -> builder.add(buffer));
		}

		@DisplayName("All command buffers in the work must submit to the same queue family")
		@Test
		void addInvalidQueueFamily() {
			final var other = new WorkQueue(new Handle(1), new Family(999, 2, Set.of()));
			final CommandPool otherPool = new Command.CommandPool(new Handle(2), dev, other);
			final CommandBuffer invalid = otherPool.primary().begin().end();
			builder.add(buffer);
			assertThrows(IllegalArgumentException.class, () -> builder.add(invalid));
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
			// Build work
			work = builder
					.add(buffer)
					.wait(wait, VkPipelineStage.TOP_OF_PIPE)
					.signal(signal)
					.build();

			// Submit work
			work.submit((Fence) null);

			// Init expected submission descriptor
			final VkSubmitInfo expected = new VkSubmitInfo() {
				@Override
				public boolean equals(Object obj) {
					final VkSubmitInfo info = (VkSubmitInfo) obj;
					assertEquals(1, info.commandBufferCount);
					assertEquals(NativeObject.array(List.of(buffer)), info.pCommandBuffers);

					// Check wait semaphores
					assertEquals(1, info.waitSemaphoreCount);
					assertEquals(NativeObject.array(Set.of(wait)), info.pWaitSemaphores);
					assertEquals(new PointerToIntArray(new int[]{VkPipelineStage.TOP_OF_PIPE.value()}), info.pWaitDstStageMask);

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
