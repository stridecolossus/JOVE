package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkCommandBufferAllocateInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferBeginInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferLevel;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Work.Batch;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

class CommandTest extends AbstractVulkanTest {
	private Command cmd;
	private Queue queue;

	@BeforeEach
	void before() {
		final Family family = new Family(0, 1, Set.of());
		queue = new Queue(new Handle(new Pointer(1)), family);
		cmd = mock(Command.class);
	}

	@Test
	void submitAndWait() {
		final Pool pool = Pool.create(dev, queue);
		cmd = spy(Command.class);
		cmd.submitAndWait(pool);
		verify(cmd).execute(eq(lib), isA(Buffer.class));
		assertEquals(0, pool.buffers().count());
	}

	@Nested
	class BufferTests {
		private Buffer buffer;
		private Pool pool;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, queue);
			buffer = pool.allocate(1, true).iterator().next();
		}

		@Test
		void constructor() {
			assertNotNull(buffer.handle());
			assertEquals(pool, buffer.pool());
			assertEquals(false, buffer.isReady());
		}

		@Test
		void begin() {
			buffer.begin();
			verify(lib).vkBeginCommandBuffer(eq(buffer), any(VkCommandBufferBeginInfo.class));
			assertEquals(false, buffer.isReady());
		}

		@Test
		void beginAlreadyRecording() {
			buffer.begin();
			assertThrows(IllegalStateException.class, () -> buffer.begin());
		}

		@Test
		void beginAlreadyRecorded() {
			buffer.begin();
			buffer.end();
			assertThrows(IllegalStateException.class, () -> buffer.begin());
		}

		@Test
		void end() {
			buffer.begin();
			buffer.end();
			verify(lib).vkEndCommandBuffer(buffer);
			assertEquals(true, buffer.isReady());
		}

		@Test
		void endNotRecording() {
			assertThrows(IllegalStateException.class, () -> buffer.end());
		}

		@Test
		void add() {
			buffer.begin();
			buffer.add(cmd);
			verify(cmd).execute(lib, buffer);
			assertEquals(false, buffer.isReady());
		}

		@Test
		void addNotRecording() {
			assertThrows(IllegalStateException.class, () -> buffer.add(mock(Command.class)));
		}

		@Test
		void reset() {
			buffer.begin();
			buffer.end();
			buffer.reset();
			assertEquals(false, buffer.isReady());
			verify(lib).vkResetCommandBuffer(buffer, 0);
		}

		@Test
		void resetBeginNextRecording() {
			buffer.begin();
			buffer.end();
			buffer.reset();
			buffer.begin();
		}

		@Test
		void resetRecording() {
			buffer.begin();
			assertThrows(IllegalStateException.class, () -> buffer.reset());
		}

		@Test
		void free() {
			buffer.free();
			// TODO
		}
	}

	@Nested
	class CommandPoolTests {
		private Pool pool;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, queue);
		}

		@Test
		void constructor() {
			assertEquals(queue, pool.queue());
			assertNotNull(pool.buffers());
			assertEquals(0, pool.buffers().count());
		}

		@Test
		void descriptor() {
			// Check allocator
			final ArgumentCaptor<VkCommandPoolCreateInfo> captor = ArgumentCaptor.forClass(VkCommandPoolCreateInfo.class);
			verify(lib).vkCreateCommandPool(eq(dev), captor.capture(), isNull(), eq(POINTER));

			// Check descriptor
			final VkCommandPoolCreateInfo info = captor.getValue();
			assertEquals(0, info.queueFamilyIndex);
			assertEquals(0, info.flags);
		}

		@Test
		void allocate() {
			// Allocate a buffer
			final List<Buffer> buffers = pool.allocate(1, false);
			assertNotNull(buffers);
			assertEquals(1, buffers.size());

			// Check allocator
			final ArgumentCaptor<VkCommandBufferAllocateInfo> captor = ArgumentCaptor.forClass(VkCommandBufferAllocateInfo.class);
			verify(lib).vkAllocateCommandBuffers(eq(dev), captor.capture(), isA(Pointer[].class));

			// Check descriptor
			final VkCommandBufferAllocateInfo info = captor.getValue();
			assertEquals(VkCommandBufferLevel.SECONDARY, info.level);
			assertEquals(1, info.commandBufferCount);
			assertEquals(pool.handle(), info.commandPool);
		}

		@Test
		void reset() {
			pool.reset();
			verify(lib).vkResetCommandPool(dev, pool, 0);
		}

		@Test
		void free() {
			final Buffer buffer = pool.allocate();
			pool.free();
			assertEquals(0, pool.buffers().count());
			final Memory array = NativeObject.array(List.of(buffer));
			verify(lib).vkFreeCommandBuffers(dev, pool, 1, array);
		}

		@Test
		void waitIdle() {
			pool.waitIdle();
			verify(lib).vkQueueWaitIdle(queue);
		}

		@Test
		void submit() {
			final Batch batch = mock(Batch.class);
			final VkSubmitInfo[] info = new VkSubmitInfo[1];
			when(batch.submit()).thenReturn(info);

			final Fence fence = mock(Fence.class);
			pool.submit(batch, fence);
			verify(lib).vkQueueSubmit(queue, 1, info, fence);
		}

		@Test
		void destroy() {
			pool.destroy();
			verify(lib).vkDestroyCommandPool(dev, pool, null);
		}
	}
}
