package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkDescriptorBufferInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.TRANSFER_SRC, VkBufferUsageFlag.UNIFORM_BUFFER);
//	private static final VkBufferUsageFlag[] FLAGS = {VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.TRANSFER_SRC, VkBufferUsageFlag.UNIFORM_BUFFER};
	private static final long SIZE = 3;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private Region region;
	private ByteBuffer bb;
	private AllocationService allocator;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));
		when(mem.size()).thenReturn(SIZE);

		// Init mapped region
		bb = mock(ByteBuffer.class);
		region = mock(Region.class);
		when(mem.map()).thenReturn(region);
		when(region.buffer()).thenReturn(bb);

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
	void validate() {
		buffer.validate(0);
		buffer.validate(2);
	}

	@Test
	void validateInvalidOffset() {
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(SIZE));
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(-1));
	}

	@Test
	void require() {
		buffer.require(FLAGS.toArray(VkBufferUsageFlag[]::new));
		buffer.require(VkBufferUsageFlag.VERTEX_BUFFER);
	}

	@Test
	void requireNotSupported() {
		assertThrows(IllegalStateException.class, () -> buffer.require(VkBufferUsageFlag.STORAGE_BUFFER));
	}

	@Test
	void buffer() {
		assertEquals(bb, buffer.buffer());
	}

	@Test
	void create() {
		final MemoryProperties<VkBufferUsageFlag> props = new MemoryProperties<>(FLAGS, VkSharingMode.EXCLUSIVE, Set.of(), Set.of());
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
		assertEquals(Set.of(VkBufferUsageFlag.TRANSFER_SRC), staging.usage());
		assertEquals(SIZE, staging.length());

		// Check data is copied to buffer
		verify(region).buffer();
		verify(data).buffer(bb);
	}

	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyBuffer(dev, buffer, null);
		verify(mem).destroy();
	}

	@Nested
	class DescriptorResourceTests {
		private DescriptorResource uniform;

		@BeforeEach
		void before() {
			uniform = buffer.resource(VkDescriptorType.UNIFORM_BUFFER, 1);
		}

		@Test
		void constructor() {
			assertNotNull(uniform);
			assertEquals(VkDescriptorType.UNIFORM_BUFFER, uniform.type());
		}

		@Test
		void invalidDescriptorType() {
			assertThrows(IllegalArgumentException.class, () -> buffer.resource(VkDescriptorType.SAMPLER, 0));
		}

		@Test
		void unsupportedBufferUsage() {
			assertThrows(IllegalStateException.class, () -> buffer.resource(VkDescriptorType.STORAGE_BUFFER, 0));
		}

		@Test
		void invalidBufferOffset() {
			assertThrows(IllegalArgumentException.class, () -> buffer.resource(VkDescriptorType.UNIFORM_BUFFER, 3));
		}

		@Test
		void populate() {
			final var info = (VkDescriptorBufferInfo) uniform.populate();
			assertEquals(buffer.handle(), info.buffer);
			assertEquals(1, info.offset);
			assertEquals(buffer.length(), info.range);
		}
	}

	@Nested
	class CommandTests {
		private Command.Buffer cb;
		private VulkanBuffer index;

		@BeforeEach
		void before() {
			index = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsageFlag.INDEX_BUFFER), mem, SIZE);
			cb = mock(Command.Buffer.class);
		}

		@Test
		void bind() {
			final Command cmd = buffer.bindVertexBuffer(2);
			assertNotNull(cmd);
			cmd.execute(lib, cb);
			verify(lib).vkCmdBindVertexBuffers(cb, 2, 1, NativeObject.array(List.of(buffer)), new long[]{0});
		}

		@Test
		void bindIndexBuffer() {
			final Command cmd = index.bindIndexBuffer(VkIndexType.UINT32, 0);
			assertNotNull(cmd);
			cmd.execute(lib, cb);
			verify(lib).vkCmdBindIndexBuffer(cb, index, 0, VkIndexType.UINT32);
		}

		@Test
		void copy() {
			final var flags = Set.of(VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.TRANSFER_DST);
			final VulkanBuffer dest = new VulkanBuffer(new Pointer(2), dev, flags, mem, SIZE);
			final Command cmd = buffer.copy(dest);
			assertNotNull(cmd);
		}
	}
}
