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
import org.sarge.jove.util.EnumMask;

import com.sun.jna.*;

class CommandTest {
	private Command cmd;
	private WorkQueue queue;
	private CommandPool pool;
	private DeviceContext dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();
		queue = new WorkQueue(new Handle(1), new Family(2, 1, Set.of()));
		pool = CommandPool.create(dev, queue);
		cmd = spy(Command.class);
	}

	@DisplayName("A command can be submitted as a one-off operation")
	@Test
	void submit() {
		final CommandBuffer buffer = cmd.submit(pool);
		verify(cmd).execute(lib, buffer);
	}

	@DisplayName("A command buffer...")
	@Nested
	class BufferTests {
		private PrimaryBuffer buffer;

		@BeforeEach
		void before() {
			buffer = pool.primary();
		}

		@Test
		void constructor() {
			assertEquals(pool, buffer.pool());
		}

		@DisplayName("can be released back to the pool")
		@Test
		void free() {
			final Memory array = NativeObject.array(List.of(buffer));
			buffer.free();
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
						assertEquals(VkCommandBufferUsage.ONE_TIME_SUBMIT.value(), info.flags.bits());
						assertEquals(null, info.pInheritanceInfo);
						return true;
					}
				};
				buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
				verify(lib).vkBeginCommandBuffer(buffer, expected);
				assertEquals(false, buffer.isReady());
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
				buffer.record(cmd);
				verify(cmd).execute(lib, buffer);
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
				buffer.begin().end();
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

			@DisplayName("can be reset")
			@Test
			void reset() {
				buffer.reset();
				buffer.begin();
				verify(lib).vkResetCommandBuffer(buffer, EnumMask.of());
				assertEquals(false, buffer.isReady());
			}
		}
	}

	@DisplayName("A secondary command buffer...")
	@Nested
	class SecondaryBufferTests {
		private SecondaryBuffer secondary;

		@BeforeEach
		void before() {
			secondary = pool.secondary();
		}

		@DisplayName("can be recorded to a primary command buffer")
		@Test
		void add() {
			// Record secondary command sequence
			final Handle pass = new Handle(1);
			secondary.begin(pass).record(cmd).end();
			assertEquals(true, secondary.isReady());

			// Record to primary buffer
			final PrimaryBuffer buffer = pool.primary();
			buffer.begin().add(List.of(secondary));
			verify(lib).vkCmdExecuteCommands(buffer, 1, NativeObject.array(List.of(secondary)));
		}

		@DisplayName("cannot be recorded to a primary command buffer if it is not ready")
		@Test
		void notReady() {
			final PrimaryBuffer buffer = pool.primary().begin();
			assertThrows(IllegalStateException.class, () -> buffer.add(List.of(secondary)));
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
					assertEquals(0, info.flags.bits());
					assertEquals(queue.family().index(), info.queueFamilyIndex);
					return true;
				}
			};
			verify(lib).vkCreateCommandPool(dev, expected, null, dev.factory().pointer());
		}

		@Test
		void allocate() {
			// Allocate a buffer
			final Collection<PrimaryBuffer> buffers = pool.primary(1);
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
		void reset() {
			pool.reset();
			verify(lib).vkResetCommandPool(dev, pool, EnumMask.of());
		}

		@Test
		void free() {
			final CommandBuffer buffer = pool.primary();
			final Memory array = NativeObject.array(List.of(buffer));
			pool.free(Set.of(buffer));
			verify(lib).vkFreeCommandBuffers(dev, pool, 1, array);
		}

		@Test
		void destroy() {
			pool.destroy();
			verify(lib).vkDestroyCommandPool(dev, pool, null);
		}
	}
}
