package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsage;
import org.sarge.jove.platform.vulkan.VkDescriptorBufferInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Resource;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsage> FLAGS = Set.of(VkBufferUsage.VERTEX_BUFFER, VkBufferUsage.TRANSFER_SRC);
	private static final long SIZE = 3;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private Region region;
	private AllocationService allocator;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));
		when(mem.size()).thenReturn(SIZE);

		// Init mapped region
		region = mock(Region.class);
		when(mem.map()).thenReturn(region);

		// Init memory allocator
		allocator = mock(AllocationService.class);
		when(allocator.allocate(isA(VkMemoryRequirements.class), isA(MemoryProperties.class))).thenReturn(mem);

		// Create buffer
		buffer = new VulkanBuffer(new Pointer(2), dev, FLAGS, mem, SIZE);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(2), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(FLAGS, buffer.usage());
		assertEquals(mem, buffer.memory());
		assertEquals(SIZE, buffer.length());
	}

	@Test
	void load() {
		// Create target memory buffer
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(region.buffer()).thenReturn(bb);

		// Load data
		final Bufferable data = mock(Bufferable.class);
		buffer.load(data);
		verify(mem).map();
		verify(data).buffer(bb);
	}

	@Test
	void create() {
		final MemoryProperties<VkBufferUsage> props = new MemoryProperties<>(FLAGS, VkSharingMode.EXCLUSIVE, Set.of(), Set.of());
		buffer = VulkanBuffer.create(dev, allocator, SIZE, props);
		assertNotNull(buffer);
		assertEquals(FLAGS, buffer.usage());
	}

	@Test
	void staging() {
		// Create data
		final Bufferable data = mock(Bufferable.class);
		when(data.length()).thenReturn((int) SIZE);

		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);
		assertNotNull(staging);
		assertEquals(Set.of(VkBufferUsage.TRANSFER_SRC), staging.usage());
		assertEquals(SIZE, staging.length());

		// Check data is copied to buffer
		verify(region).buffer();
		verify(data).buffer(null);
	}

	@Test
	void close() {
		buffer.close();
		verify(lib).vkDestroyBuffer(dev, buffer, null);
		verify(mem).close();
	}

	@Nested
	class CommandTests {
		private Command.Buffer cb;
		private VulkanBuffer dest;
		private VulkanBuffer index;

		@BeforeEach
		void before() {
			final var flags = Set.of(VkBufferUsage.VERTEX_BUFFER, VkBufferUsage.TRANSFER_DST);
			dest = new VulkanBuffer(new Pointer(2), dev, flags, mem, SIZE);
			index = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsage.INDEX_BUFFER), mem, SIZE);
			cb = mock(Command.Buffer.class);
		}

		@Test
		void bind() {
			final Command cmd = buffer.bindVertexBuffer();
			assertNotNull(cmd);
			cmd.execute(lib, cb);
			verify(lib).vkCmdBindVertexBuffers(cb, 0, 1, NativeObject.toArray(List.of(buffer)), new long[]{0});
		}

		@Test
		void bindIndexBuffer() {
			final Command cmd = index.bindIndexBuffer(VkIndexType.UINT32);
			assertNotNull(cmd);
			cmd.execute(lib, cb);
			verify(lib).vkCmdBindIndexBuffer(cb, index, 0, VkIndexType.UINT32);
		}

		@Test
		void copy() {
			// Execute copy command
			final Command cmd = buffer.copy(dest);
			assertNotNull(cmd);
			cmd.execute(lib, cb);

			// Check API
			final ArgumentCaptor<VkBufferCopy[]> captor = ArgumentCaptor.forClass(VkBufferCopy[].class);
			verify(lib).vkCmdCopyBuffer(eq(cb), eq(buffer), eq(dest), eq(1), captor.capture());

			// Check region
			final VkBufferCopy[] array = captor.getValue();
			assertNotNull(array);
			assertEquals(1, array.length);
			assertEquals(SIZE, array[0].size);
		}

		@Test
		void copyTooSmall() {
			final DeviceMemory small = mock(DeviceMemory.class);
			final VulkanBuffer dest = mock(VulkanBuffer.class);
			when(dest.memory()).thenReturn(small);
			when(small.size()).thenReturn(SIZE - 1);
			assertThrows(IllegalStateException.class, "Destination buffer is too small", () -> buffer.copy(dest));
		}

		@Test
		void copyInvalidDirection() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> dest.copy(buffer));
		}

		@Test
		void copySelf() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.copy(buffer));
		}

		@Test
		void copyInvalidUsage() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.copy(index));
		}
	}

	@Nested
	class UniformBufferResourceTests {
		private Resource uniform;

		@BeforeEach
		void before() {
			final VulkanBuffer vbo = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsage.UNIFORM_BUFFER), mem, SIZE);
			uniform = vbo.uniform();
		}

		@Test
		void constructor() {
			assertNotNull(uniform);
			assertEquals(VkDescriptorType.UNIFORM_BUFFER, uniform.type());
		}

		@Test
		void populate() {
			// Populate write descriptor
			final var write = new VkWriteDescriptorSet();
			uniform.populate(write);

			// Check descriptor
			final VkDescriptorBufferInfo info = write.pBufferInfo;
			assertNotNull(info);
			assertEquals(buffer.handle(), info.buffer);
			assertEquals(buffer.memory().size(), info.range);
			assertEquals(0, info.offset);
		}

		@Test
		void uniformInvalidBuffer() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.uniform());
		}
	}
}
