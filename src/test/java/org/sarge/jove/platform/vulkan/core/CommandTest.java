package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkCommandBufferAllocateInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferBeginInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferLevel;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateInfo;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

class CommandTest extends AbstractVulkanTest {
	private Command cmd;
	private Queue queue;

	@BeforeEach
	void before() {
		// Create queue family
		final Queue.Family family = mock(Queue.Family.class);
		when(family.index()).thenReturn(0);

		// Create queue
		queue = mock(Queue.class);
		when(queue.family()).thenReturn(family);
		when(queue.device()).thenReturn(dev);

		// Create command
		cmd = mock(Command.class);
	}

	@Test
	void once() {
		final Pool pool = Pool.create(queue);
		final Buffer buffer = Command.once(pool, cmd);
		assertEquals(true, buffer.isReady());
		verify(lib).vkBeginCommandBuffer(eq(buffer.handle()), any(VkCommandBufferBeginInfo.class));
		verify(cmd).execute(lib, buffer.handle());
		verify(lib).vkEndCommandBuffer(buffer.handle());
	}

	@Nested
	class BufferTests {
		private Buffer buffer;
		private Pool pool;

		@BeforeEach
		void before() {
			pool = Pool.create(queue);
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
			verify(lib).vkBeginCommandBuffer(eq(buffer.handle()), any(VkCommandBufferBeginInfo.class));
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
			verify(lib).vkEndCommandBuffer(buffer.handle());
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
			verify(cmd).execute(lib, buffer.handle());
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
			verify(lib).vkResetCommandBuffer(buffer.handle(), 0);
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
			pool = Pool.create(queue);
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
			final PointerByReference handle = lib.factory().pointer();
			verify(lib).vkCreateCommandPool(eq(dev.handle()), captor.capture(), isNull(), eq(handle));

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
			verify(lib).vkAllocateCommandBuffers(eq(dev.handle()), captor.capture(), isA(Pointer[].class));

			// Check descriptor
			final VkCommandBufferAllocateInfo info = captor.getValue();
			assertEquals(VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_SECONDARY, info.level);
			assertEquals(1, info.commandBufferCount);
			assertEquals(pool.handle(), info.commandPool);
		}

		@Test
		void reset() {
			pool.reset();
			verify(lib).vkResetCommandPool(dev.handle(), pool.handle(), 0);
		}

		@Test
		void free() {
			pool.allocate();
			pool.free();
			assertEquals(0, pool.buffers().count());
			verify(lib).vkFreeCommandBuffers(dev.handle(), pool.handle(), 1, factory.pointers);
		}

		@Test
		void destroy() {
			final Handle handle = pool.handle();
			pool.destroy();
			verify(lib).vkDestroyCommandPool(dev.handle(), handle, null);
		}
	}
}
