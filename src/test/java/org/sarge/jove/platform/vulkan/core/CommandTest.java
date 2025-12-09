package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkCommandBufferResetFlags.RELEASE_RESOURCES;
import static org.sarge.jove.platform.vulkan.VkCommandBufferUsageFlags.RENDER_PASS_CONTINUE;
import static org.sarge.jove.platform.vulkan.core.Command.Buffer.Stage.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer.Stage;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class CommandTest {
	private static class MockCommandLibrary extends MockVulkanLibrary {
		private boolean destroyed;
		private boolean reset;
		private boolean free;
		private Stage stage = INITIAL;

		@Override
		public VkResult vkCreateCommandPool(LogicalDevice device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pCommandPool) {
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroyCommandPool(LogicalDevice device, Pool commandPool, Handle pAllocator) {
			assertNotNull(device);
			assertNotNull(commandPool);
			assertEquals(null, pAllocator);
			destroyed = true;
		}

		@Override
		public VkResult vkResetCommandPool(LogicalDevice device, Pool commandPool, EnumMask<VkCommandPoolResetFlags> flags) {
			assertNotNull(device);
			assertNotNull(commandPool);
			assertEquals(new EnumMask<>(VkCommandPoolResetFlags.RELEASE_RESOURCES), flags);
			reset = true;
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, Handle[] pCommandBuffers) {
			assertNotNull(device);
			assertNotNull(pAllocateInfo.commandPool);
			assertEquals(1, pAllocateInfo.commandBufferCount);
			assertEquals(1, pCommandBuffers.length);
			pCommandBuffers[0] = new Handle(4);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkFreeCommandBuffers(LogicalDevice device, Pool commandPool, int commandBufferCount, Buffer[] pCommandBuffers) {
			assertNotNull(device);
			assertNotNull(commandPool);
			assertEquals(1, commandBufferCount);
			assertEquals(1, pCommandBuffers.length);
			assertNotNull(pCommandBuffers[0]);
			free = true;
		}

		@Override
		public VkResult vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
			if(commandBuffer.isPrimary()) {
				assertEquals(new EnumMask<>(), pBeginInfo.flags);
				assertEquals(null, pBeginInfo.pInheritanceInfo);
			}
			else {
				assertEquals(new EnumMask<>(RENDER_PASS_CONTINUE), pBeginInfo.flags);
				assertNotNull(pBeginInfo.pInheritanceInfo);
			}
			assertEquals(INITIAL, commandBuffer.stage());
			stage = RECORDING;
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkEndCommandBuffer(Buffer commandBuffer) {
			assertEquals(RECORDING, commandBuffer.stage());
			stage = EXECUTABLE;
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkResetCommandBuffer(Buffer commandBuffer, EnumMask<VkCommandBufferResetFlags> flags) {
			assertEquals(new EnumMask<>(RELEASE_RESOURCES), flags);
			assertEquals(EXECUTABLE, commandBuffer.stage());
			stage = INITIAL;
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkCmdExecuteCommands(Buffer commandBuffer, int commandBufferCount, Buffer[] pCommandBuffers) {
			assertEquals(true, commandBuffer.isPrimary());
			assertEquals(RECORDING, commandBuffer.stage());
			if(commandBufferCount > 0) {
				assertEquals(false, pCommandBuffers[0].isPrimary());
				assertEquals(EXECUTABLE, pCommandBuffers[0].stage());
			}
		}
	}

	private MockCommand command;
	private WorkQueue queue;
	private Pool pool;
	private LogicalDevice device;
	private MockCommandLibrary library;

	@BeforeEach
	void before() {
		command = new MockCommand();
		library = new MockCommandLibrary();
		device = new MockLogicalDevice(library);
		queue = new WorkQueue(new Handle(1), new Family(0, 1, Set.of()));
		pool = new Pool(new Handle(2), device, queue);
	}

	@Nested
	class BufferTest {
		private Buffer buffer;

		@BeforeEach
		void before() {
			buffer = pool.allocate(1, true).getFirst();
		}

		@Nested
		class Initial {
			@Test
			void ready() {
				assertEquals(false, buffer.isReady());
			}

			@Test
			void begin() {
				buffer.begin();
			}

			@Test
			void add() {
				assertThrows(IllegalStateException.class, () -> buffer.add(command));
				assertThrows(IllegalStateException.class, () -> buffer.add(List.of()));
			}

			@Test
			void end() {
				assertThrows(IllegalStateException.class, () -> buffer.end());
			}

			@Test
			void reset() {
				assertThrows(IllegalStateException.class, () -> buffer.reset(RELEASE_RESOURCES));
			}
		}

		@Nested
		class Recording {
			@BeforeEach
			void before() {
				buffer.begin();
			}

			@Test
			void ready() {
				assertEquals(false, buffer.isReady());
			}

			@Test
			void begin() {
				assertThrows(IllegalStateException.class, () -> buffer.begin());
			}

			@Test
			void add() {
				buffer.add(command);
				buffer.add(List.of());
			}

			@Test
			void end() {
				buffer.end();
			}

			@Test
			void reset() {
				assertThrows(IllegalStateException.class, () -> buffer.reset(RELEASE_RESOURCES));
			}
		}

		@Nested
		class Executable {
			@BeforeEach
			void before() {
				buffer.begin();
				buffer.end();
			}

			@Test
			void ready() {
				assertEquals(true, buffer.isReady());
			}

			@Test
			void begin() {
				assertThrows(IllegalStateException.class, () -> buffer.end());
			}

			@Test
			void add() {
				assertThrows(IllegalStateException.class, () -> buffer.add(command));
				assertThrows(IllegalStateException.class, () -> buffer.add(List.of()));
			}

			@Test
			void end() {
				assertThrows(IllegalStateException.class, () -> buffer.end());
			}

			@Test
			void reset() {
				buffer.reset(RELEASE_RESOURCES);
				assertEquals(Stage.INITIAL, library.stage);
			}
		}
	}

	@Nested
	class SecondaryBufferTest {
		private Buffer primary, secondary;

		@BeforeEach
		void before() {
			primary = pool.allocate(1, true).getFirst();
			primary.begin();
			secondary = pool.allocate(1, false).getFirst();
		}

		@Test
		void begin() {
			secondary.begin(new VkCommandBufferInheritanceInfo(), Set.of(RENDER_PASS_CONTINUE));
		}

		@Test
		void inheritance() {
			assertThrows(IllegalArgumentException.class, () -> secondary.begin(RENDER_PASS_CONTINUE));
		}

		@Test
		void add() {
			secondary.begin(new VkCommandBufferInheritanceInfo(), Set.of(RENDER_PASS_CONTINUE));
			secondary.end();
			primary.add(List.of(secondary));
		}

		@Test
		void ready() {
			assertThrows(IllegalStateException.class, () -> primary.add(List.of(secondary)));
		}

		@Test
		void primary() {
			final Buffer invalid = pool
					.allocate(1, true)
					.getFirst()
					.begin()
					.end();

			assertThrows(IllegalArgumentException.class, () -> primary.add(List.of(invalid)));
		}
	}

	@Nested
	class PoolTest {
		@Test
		void empty() {
			assertEquals(0, pool.buffers().size());
		}

		@Test
		void allocate() {
			final List<Buffer> buffers = pool.allocate(1, true);
			assertEquals(1, buffers.size());
			assertEquals(buffers, pool.buffers());

			final Buffer buffer = buffers.getFirst();
			assertEquals(pool, buffer.pool());
			assertEquals(new Handle(4), buffer.handle());
			assertEquals(false, buffer.isReady());
			assertEquals(INITIAL, buffer.stage());
			assertEquals(true, buffer.isPrimary());
		}

		@Test
		void secondary() {
			final Buffer secondary = pool.allocate(1, false).getFirst();
			assertEquals(pool, secondary.pool());
			assertEquals(false, secondary.isReady());
			assertEquals(false, secondary.isPrimary());
			assertEquals(1, pool.buffers().size());
		}

		@Test
		void reset() {
			final Buffer buffer = pool
					.allocate(1, true)
					.getFirst()
					.begin();

			pool.reset(VkCommandPoolResetFlags.RELEASE_RESOURCES);
			assertEquals(true, library.reset);
			assertEquals(Stage.INITIAL, buffer.stage());
		}

		@Test
		void free() {
			final Buffer buffer = pool.allocate(1, true).getFirst();
			pool.free(List.of(buffer));
			assertEquals(true, library.free);
			assertEquals(Stage.INVALID, buffer.stage());
			assertEquals(0, pool.buffers().size());
		}

		@Test
		void destroy() {
			final Buffer buffer = pool.allocate(1, true).getFirst();
			pool.destroy();
			assertEquals(true, pool.isDestroyed());
			assertEquals(true, library.destroyed);
			assertEquals(Stage.INVALID, buffer.stage());
			assertEquals(0, pool.buffers().size());
		}
	}
}
