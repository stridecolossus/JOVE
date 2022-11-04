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
	private Command cmd;
	private Queue queue;
	private Pool pool;

	@BeforeEach
	void before() {
		queue = new Queue(new Handle(1), new Family(2, 1, Set.of()));
		pool = Pool.create(dev, queue);
		cmd = mock(Command.class);
	}

	@DisplayName("An immediate command...")
	@Nested
	class ImmediateCommandTests {
		@DisplayName("can be submitted as a one-off operation")
		@Test
		void submit() {
			final var immediate = spy(ImmediateCommand.class);
			final Buffer buffer = immediate.submit(pool);
			verify(immediate).record(lib, buffer);
		}
	}

	@DisplayName("A command buffer...")
	@Nested
	class BufferTests {
		private Buffer buffer;

		@BeforeEach
		void before() {
			buffer = pool.allocate();
		}

		@Test
		void constructor() {
			assertEquals(pool, buffer.pool());
			assertEquals(true, buffer.isPrimary());
		}

		@DisplayName("can be released back to the pool")
		@Test
		void free() {
			buffer.free();
			final Memory array = NativeObject.array(List.of(buffer));
			verify(lib).vkFreeCommandBuffers(dev, pool, 1, array);
		}

		@DisplayName("that is newly allocated...")
		@Nested
		class New {
			@DisplayName("is not ready for submission")
			@Test
			void isReady() {
				assertEquals(false, buffer.isReady());
			}

			@DisplayName("can begin recording")
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

			@DisplayName("cannot record commands until recording has been started")
			@Test
			void add() {
				assertThrows(IllegalStateException.class, () -> buffer.add(cmd));
			}

			@DisplayName("cannot end recording if it has not been started")
			@Test
			void end() {
				assertThrows(IllegalStateException.class, () -> buffer.end());
			}

			@DisplayName("cannot be reset")
			@Test
			void reset() {
				assertThrows(IllegalStateException.class, () -> buffer.reset());
			}
		}

		@DisplayName("that is being recorded...")
		@Nested
		class Recording {
			@BeforeEach
			void before() {
				buffer.begin();
			}

			@DisplayName("is not ready for submission")
			@Test
			void isReady() {
				assertEquals(false, buffer.isReady());
			}

			@DisplayName("cannot begin recording again until recording has finished")
			@Test
			void begin() {
				assertThrows(IllegalStateException.class, () -> buffer.begin());
			}

			@DisplayName("can record commands")
			@Test
			void add() {
				buffer.add(cmd);
				verify(cmd).record(lib, buffer);
				assertEquals(false, buffer.isReady());
			}

			@DisplayName("can end recording")
			@Test
			void end() {
				buffer.end();
				verify(lib).vkEndCommandBuffer(buffer);
				assertEquals(true, buffer.isReady());
			}

			@DisplayName("cannot be reset")
			@Test
			void reset() {
				assertThrows(IllegalStateException.class, () -> buffer.reset());
			}
		}

		@DisplayName("that has been recorded...")
		@Nested
		class Ready {
			@BeforeEach
			void before() {
				buffer.begin();
				buffer.end();
			}

			@DisplayName("is ready for submission")
			@Test
			void isReady() {
				assertEquals(true, buffer.isReady());
			}

			@DisplayName("cannot be re-recorded unless is has been reset")
			@Test
			void begin() {
				assertThrows(IllegalStateException.class, () -> buffer.begin());
			}

			@DisplayName("cannot record any more commands")
			@Test
			void add() {
				assertThrows(IllegalStateException.class, () -> buffer.add(cmd));
			}

			@DisplayName("cannot end recording")
			@Test
			void end() {
				assertThrows(IllegalStateException.class, () -> buffer.end());
			}

			@DisplayName("can be reset")
			@Test
			void reset() {
				buffer.reset();
				buffer.begin();
				verify(lib).vkResetCommandBuffer(buffer, 0);
				assertEquals(false, buffer.isReady());
			}
		}
	}

	@DisplayName("A secondary command buffer...")
	@Nested
	class SecondaryBufferTests {
		private SecondaryBuffer sec;

		@BeforeEach
		void before() {
			sec = pool.secondary(1).iterator().next();
		}

		@DisplayName("is initially not ready for submission")
		@Test
		void constructor() {
			assertEquals(pool, sec.pool());
			assertEquals(false, sec.isReady());
			assertEquals(false, sec.isPrimary());
		}

		@DisplayName("can be recorded to a primary command buffer")
		@Test
		void add() {
			// Record secondary command sequence
			sec.begin();
			sec.add(cmd);
			sec.end();
			assertEquals(true, sec.isReady());

			// Record to primary buffer
			final Buffer buffer = pool.allocate();
			buffer.begin();
			buffer.add(List.of(sec));
			verify(lib).vkCmdExecuteCommands(buffer, 1, NativeObject.array(List.of(sec)));
		}

		@DisplayName("cannot be recorded to a primary command buffer if it is not ready")
		@Test
		void notReady() {
			final Buffer buffer = pool.allocate();
			buffer.begin();
			assertThrows(IllegalStateException.class, () -> buffer.add(List.of(sec)));
		}

		@DisplayName("cannot record further secondary command buffers")
		@Test
		void invalid() {
			assertThrows(UnsupportedOperationException.class, () -> sec.add(List.of()));
		}
	}

	// TODO - doc
	@Nested
	class PoolTests {
		@Test
		void constructor() {
			assertEquals(queue, pool.queue());
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
			final Collection<Buffer> buffers = pool.allocate(1);
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
			final Collection<SecondaryBuffer> buffers = pool.secondary(1);
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
			pool.free(Set.of(buffer));
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
