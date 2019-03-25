package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.Command.Buffer;
import org.sarge.jove.platform.vulkan.Command.Pool;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;

import com.sun.jna.Pointer;

public class CommandTest extends AbstractVulkanTest {
	@Nested
	class CommandBufferTests {
		private Buffer buffer;
		private Pointer handle;

		@BeforeEach
		public void before() {
			handle = mock(Pointer.class);
			buffer = new Buffer(handle, library);
		}

		@Test
		public void constructor() {
			assertEquals(handle, buffer.handle());
			assertEquals(false, buffer.isReady());
		}

		@Test
		public void begin() {
			buffer.begin();
			verify(library).vkBeginCommandBuffer(eq(handle), any(VkCommandBufferBeginInfo.class));
			assertEquals(false, buffer.isReady());
		}

		@Test
		public void beginAlreadyRecording() {
			buffer.begin();
			assertThrows(IllegalStateException.class, () -> buffer.begin());
		}

		@Test
		public void beginAlreadyRecorded() {
			buffer.begin();
			buffer.end();
			assertThrows(IllegalStateException.class, () -> buffer.begin());
		}

		@Test
		public void end() {
			buffer.begin();
			buffer.end();
			verify(library).vkEndCommandBuffer(handle);
			assertEquals(true, buffer.isReady());
		}

		@Test
		public void endNotRecording() {
			assertThrows(IllegalStateException.class, () -> buffer.end());
		}

		@Test
		public void add() {
			final Command cmd = mock(Command.class);
			buffer.begin();
			buffer.add(cmd);
			verify(cmd).execute(library, handle);
			assertEquals(false, buffer.isReady());
		}

		@Test
		public void addNotRecording() {
			assertThrows(IllegalStateException.class, () -> buffer.add(mock(Command.class)));
		}

		@Test
		public void reset() {
			buffer.begin();
			buffer.end();
			buffer.reset();
			assertEquals(false, buffer.isReady());
			verify(library).vkResetCommandBuffer(handle, 0);
		}

		@Test
		public void resetBeginNextRecording() {
			buffer.begin();
			buffer.end();
			buffer.reset();
			buffer.begin();
		}

		@Test
		public void resetRecording() {
			buffer.begin();
			assertThrows(IllegalStateException.class, () -> buffer.reset());
		}
	}

	@Nested
	class CommandPoolTests {
		private Pool pool;
		private QueueFamily family;
		private LogicalDevice dev;

		@BeforeEach
		public void before() {
			family = mock(QueueFamily.class);
			dev = mock(LogicalDevice.class);
			pool = new Pool(new Pointer(42), Destructor.NULL, dev, library);
		}

		@Test
		public void constructor() {
			assertNotNull(pool.buffers());
			assertEquals(0, pool.buffers().count());
		}

		@Test
		public void allocate() {
			// Allocate a buffer
			final List<Buffer> buffers = pool.allocate(1, true);
			assertNotNull(buffers);
			assertEquals(1, buffers.size());

			// Check allocation
			final VkCommandBufferAllocateInfo info = new VkCommandBufferAllocateInfo();
			info.level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
			info.commandBufferCount = 1;
			info.commandPool = pool.handle();
			verify(library).vkAllocateCommandBuffers(eq(dev.handle()), argThat(structure(info)), eq(factory.pointers(1)));
		}

		@Test
		public void reset() {
			pool.reset();
			verify(library).vkResetCommandPool(dev.handle(), pool.handle(), 0);
		}

		@Test
		public void free() {
			final var buffers = pool.allocate(1, true);
			final Command.Buffer b = buffers.iterator().next();
			pool.free();
			assertEquals(0, pool.buffers().count());
			verify(library).vkFreeCommandBuffers(dev.handle(), pool.handle(), 1, new Pointer[]{b.handle()});
		}

		@Test
		public void create() {
			// Create pool
			pool = Pool.create(dev, family, VkCommandPoolCreateFlag.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
			pool.allocate(1, true);
			assertNotNull(pool);
			assertEquals(1, pool.buffers().count());

			// Check initialisation
			final VkCommandPoolCreateInfo info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = 0;
			info.flags = VkCommandPoolCreateFlag.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT.value();
			verify(library).vkCreateCommandPool(eq(dev.handle()), argThat(structure(info)), isNull(), eq(factory.reference()));

			// Destroy pool
			final Pointer handle = pool.handle();
			pool.destroy();
			assertEquals(0, pool.buffers().count());
			verify(library).vkDestroyCommandPool(dev.handle(), handle, null);
		}
	}
}