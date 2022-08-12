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

import com.sun.jna.*;

class CommandTest extends AbstractVulkanTest {
	private Queue queue;
	private Pool pool;

	@BeforeEach
	void before() {
		queue = new Queue(new Handle(1), new Family(2, 1, Set.of()));
		pool = Pool.create(dev, queue);
	}

	@Nested
	class ImmediateCommandTests {
		@Test
		void submit() {
			final ImmediateCommand cmd = spy(ImmediateCommand.class);
			final Buffer buffer = cmd.submit(pool);
			verify(cmd).execute(lib, buffer);
			assertEquals(0, pool.buffers().count());
		}
	}

	@Nested
	class BufferTests {
		private Buffer buffer;

		@BeforeEach
		void before() {
			buffer = pool.allocate();
		}

		@Test
		void constructor() {
			assertNotNull(buffer.handle());
			assertEquals(pool, buffer.pool());
			assertEquals(false, buffer.isReady());
			assertEquals(true, buffer.isPrimary());
		}

		@Test
		void begin() {
			final var expected = new VkCommandBufferBeginInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkCommandBufferBeginInfo) obj;
					assertEquals(VkCommandBufferUsage.ONE_TIME_SUBMIT.value(), info.flags);
					assertEquals(null, info.pInheritanceInfo);
					return true;
				}
			};
			buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
			verify(lib).vkBeginCommandBuffer(buffer, expected);
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
			final Command cmd = mock(Command.class);
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
		void secondary() {
			final List<SecondaryBuffer> sec = pool.secondary(1);
			buffer.begin();
			buffer.add(sec);
			verify(lib).vkCmdExecuteCommands(buffer, 1, NativeObject.array(sec));
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
			assertEquals(0, pool.buffers().count());
		}
	}

	@Nested
	class SecondaryBufferTests {
		private SecondaryBuffer sec;

		@BeforeEach
		void before() {
			sec = pool.secondary(1).get(0);
		}

		@Test
		void constructor() {
			assertNotNull(sec);
			assertEquals(pool, sec.pool());
			assertEquals(false, sec.isReady());
			assertEquals(false, sec.isPrimary());
		}

		@Test
		void executeSecondary() {
			assertThrows(UnsupportedOperationException.class, () -> sec.add(List.of()));
		}
	}

	@Nested
	class CommandPoolTests {
		@Test
		void constructor() {
			assertEquals(queue, pool.queue());
			assertNotNull(pool.buffers());
			assertEquals(0, pool.buffers().count());
		}

		@Test
		void descriptor() {
			final var expected = new VkCommandPoolCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkCommandPoolCreateInfo) obj;
					assertEquals(0, info.flags);
					assertEquals(queue.family().index(), info.queueFamilyIndex);
					return true;
				}
			};
			verify(lib).vkCreateCommandPool(dev, expected, null, factory.pointer());
		}

		@Test
		void allocate() {
			// Allocate a buffer
			final List<Buffer> buffers = pool.allocate(1);
			assertNotNull(buffers);
			assertEquals(1, buffers.size());

			// Check allocator
			final var expected = new VkCommandBufferAllocateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkCommandBufferAllocateInfo) obj;
					assertEquals(VkCommandBufferLevel.PRIMARY, info.level);
					assertEquals(1, info.commandBufferCount);
					assertEquals(pool.handle(), info.commandPool);
					return true;
				}
			};
			verify(lib).vkAllocateCommandBuffers(dev, expected, new Pointer[1]);
		}

		@Test
		void secondary() {
			final List<SecondaryBuffer> buffers = pool.secondary(1);
			assertNotNull(buffers);
			assertEquals(1, buffers.size());
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
		void destroy() {
			pool.destroy();
			verify(lib).vkDestroyCommandPool(dev, pool, null);
		}
	}
}
