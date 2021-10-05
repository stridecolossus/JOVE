package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class WorkTest extends AbstractVulkanTest {
	private Work work;
	private Queue queue;
	private Command.Pool pool;
	private Command.Buffer buffer;

	@BeforeEach
	void before() {
		// Create queue
		final Queue.Family family = new Queue.Family(0, 1, Set.of());
		queue = new Queue(new Handle(new Pointer(1)), family);

		// Create pool
		pool = mock(Command.Pool.class);
		when(pool.queue()).thenReturn(queue);

		// Create command buffer
		buffer = mock(Command.Buffer.class);
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(new Pointer(2)));
		when(buffer.isReady()).thenReturn(true);

		// Create work
		work = new Work.Builder(queue).add(buffer).build();
	}

	@Test
	void constructor() {
		assertEquals(queue, work.queue());
	}

	@Test
	void submit() {
		work.submit(null, lib);
		check(null);
	}

	@Test
	void submitFence() {
		final Fence fence = mock(Fence.class);
		when(fence.handle()).thenReturn(new Handle(new Pointer(2)));
		work.submit(fence, lib);
		check(fence.handle());
	}

	private VkSubmitInfo check(Handle fence) {
		// Check API
		final ArgumentCaptor<VkSubmitInfo[]> captor = ArgumentCaptor.forClass(VkSubmitInfo[].class);
		verify(lib).vkQueueSubmit(eq(queue.handle()), eq(1), captor.capture(), eq(fence));

		// Check submit
		final VkSubmitInfo[] array = captor.getValue();
		assertNotNull(array);
		assertEquals(1, array.length);

		// Check descriptor
		final VkSubmitInfo info = array[0];
		assertNotNull(info);
		assertEquals(1, info.commandBufferCount);
		assertNotNull(info.pCommandBuffers);
		assertEquals(0, info.waitSemaphoreCount);
		assertEquals(null, info.pWaitSemaphores);
		assertEquals(null, info.pWaitDstStageMask);
		assertEquals(0, info.signalSemaphoreCount);
		assertEquals(null, info.pSignalSemaphores);
		return info;
	}

	@Test
	void submitQueueMismatch() {
		final Queue q = new Queue(new Handle(new Pointer(2)), new Queue.Family(999, 1, Set.of()));
		final Work other = new Work(new VkSubmitInfo(), q);
		assertThrows(IllegalArgumentException.class, () -> Work.submit(List.of(work, other), null, lib));
	}

	@Nested
	class ImmediateCommandTests {
		@BeforeEach
		void before() {
			when(pool.allocate()).thenReturn(buffer);
			when(buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)).thenReturn(buffer);
			when(buffer.end()).thenReturn(buffer);
		}

		@Test
		void submitOneTimeCommand() {
			final Command cmd = mock(Command.class);
			when(buffer.add(cmd)).thenReturn(buffer);
			Work.submit(cmd, pool);
		}
	}

	@Nested
	class BuilderTests {
		private Work.Builder builder;

		@BeforeEach
		void before() {
			builder = new Work.Builder(queue);
		}

		@Test
		void build() {
			// Create semaphore
			final Semaphore semaphore = mock(Semaphore.class);
			when(semaphore.handle()).thenReturn(new Handle(new Pointer(3)));

			// Build work
			final Work work = builder
					.add(buffer)
					.wait(semaphore, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
					.signal(semaphore)
					.build();

			// Check work
			assertNotNull(work);
			assertEquals(queue, work.queue());
		}

		@Test
		void buildEmpty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildBufferNotReady() {
			when(buffer.isReady()).thenReturn(false);
			assertThrows(IllegalStateException.class, () -> builder.add(buffer));
		}

		@Test
		void buildBufferQueueMismatch() {
			final Queue other = new Queue(new Handle(new Pointer(2)), new Queue.Family(999, 1, Set.of()));
			builder.add(buffer);
			when(buffer.pool().queue()).thenReturn(other);
			assertThrows(IllegalArgumentException.class, () -> builder.add(buffer));
		}

		@Test
		void buildEmptySemaphorePipelineStages() {
			assertThrows(IllegalArgumentException.class, () -> builder.wait(mock(Semaphore.class), Set.of()));
		}
	}
}
