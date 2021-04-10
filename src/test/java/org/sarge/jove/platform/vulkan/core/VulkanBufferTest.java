package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.DeviceMemory;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);

	private VulkanBuffer buffer;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));

		// Create buffer
		buffer = new VulkanBuffer(new Pointer(2), dev, FLAGS, 3, mem);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(2)), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(3, buffer.length());
		assertEquals(FLAGS, buffer.usage());
	}

	@Nested
	class CommandTests {
		private Handle cmdHandle;
		private VulkanBuffer dest;
		private VulkanBuffer index;

		@BeforeEach
		void before() {
			final var flags = Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			dest = new VulkanBuffer(new Pointer(2), dev, flags, 3, mem);
			index = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT), 3, mem);
			cmdHandle = new Handle(new Pointer(42));
		}

		@Test
		void bind() {
			final Command cmd = buffer.bindVertexBuffer();
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);
			verify(lib).vkCmdBindVertexBuffers(eq(cmdHandle), eq(0), eq(1), isA(Pointer.class), eq(new long[]{0}));
		}

		@Test
		void bindIndexBuffer() {
			final Command cmd = index.bindIndexBuffer();
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);
			verify(lib).vkCmdBindIndexBuffer(cmdHandle, index.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
		}

		@Test
		void copy() {
			// Execute copy command
			final Command cmd = buffer.copy(dest);
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);

			// Check API
			final ArgumentCaptor<VkBufferCopy[]> captor = ArgumentCaptor.forClass(VkBufferCopy[].class);
			verify(lib).vkCmdCopyBuffer(eq(cmdHandle), eq(buffer.handle()), eq(dest.handle()), eq(1), captor.capture());

			// Check region
			final VkBufferCopy[] array = captor.getValue();
			assertNotNull(array);
			assertEquals(1, array.length);
			assertEquals(3, array[0].size);
		}

		@Test
		void copyTooSmall() {
			final VulkanBuffer dest = mock(VulkanBuffer.class);
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

	@Test
	void load() {
		// Over-ride factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(lib.factory()).thenReturn(factory);

		// Init buffer memory
		final PointerByReference ref = mock(PointerByReference.class);
		final Pointer data = mock(Pointer.class);
		when(factory.pointer()).thenReturn(ref);
		when(ref.getValue()).thenReturn(data);

//		// Init internal buffer
//		final ByteBuffer bb = mock(ByteBuffer.class);
//		when(data.getByteBuffer(0, 3)).thenReturn(bb);

		// Load buffer
		final Bufferable obj = mock(Bufferable.class);
		when(obj.length()).thenReturn(3);
		buffer.load(obj);

		// Check memory is mapped
		verify(lib).vkMapMemory(dev.handle(), mem.handle(), 0, 3L, 0, ref);
		verify(lib).vkUnmapMemory(dev.handle(), mem.handle());

		// TODO
//		// Load bufferable at offset
//		final Bufferable obj = mock(Bufferable.class);
//		when(data.getByteBuffer(0, 1)).thenReturn(bb);
//		buffer.load(obj, 2);
//		verify(obj).buffer(bb);
	}

	@Test
	void loadBufferTooLarge() {
		final Bufferable obj = mock(Bufferable.class);
		when(obj.length()).thenReturn(999);
		assertThrows(IllegalStateException.class, () -> buffer.load(obj));
	}

	@Test
	void loadInvalidOffset() {
		final Bufferable obj = mock(Bufferable.class);
		when(obj.length()).thenReturn(1);
		assertThrows(IllegalStateException.class, () -> buffer.load(obj, 3));
	}

	@Test
	void destroy() {
//		final Handle handle = buffer.handle();
		buffer.destroy();
//		verify(lib).vkFreeMemory(dev.handle(), mem, null);
		verify(lib).vkDestroyBuffer(dev.handle(), buffer.handle(), null);
		verify(mem).destroy();
	}

	@Nested
	class UniformBufferResourceTests {
		private DescriptorSet.Resource uniform;

		@BeforeEach
		void before() {
			final VulkanBuffer vbo = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT), 3, mem);
			uniform = vbo.uniform();
		}

		@Test
		void constructor() {
			assertNotNull(uniform);
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, uniform.type());
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
			assertEquals(buffer.length(), info.range);
			assertEquals(0, info.offset);
		}

		@Test
		void uniformInvalidBuffer() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.uniform());
		}
	}

	@Nested
	class BuilderTests {
		private VulkanBuffer.Builder builder;
		private VulkanAllocator.Request allocation;

		@BeforeEach
		void before() {
			// Init VBO memory allocation
			// Init image memory
//			final Pointer ptr = new Pointer(3);
//			final DeviceMemory mem = mock(DeviceMemory.class);
//			when(mem.memory()).thenReturn(ptr);
			final VulkanAllocator allocator = mock(VulkanAllocator.class);
			allocation = mock(VulkanAllocator.Request.class);
			when(dev.allocator()).thenReturn(allocator);
			when(allocator.request()).thenReturn(allocation);
			when(allocation.allocate()).thenReturn(mem);
			when(allocation.init(any())).thenReturn(allocation);
			//when(allocation.size()).thenReturn(4L);

			// Create builder
			builder = new VulkanBuffer.Builder(dev);
		}

		@Test
		void build() {
			// Build buffer
			buffer = builder
					.length(4)
					.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
					.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
					.mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE)
					.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
					.build();

			// Check buffer
			assertNotNull(buffer);
			assertNotNull(buffer.handle());
			assertEquals(dev, buffer.device());

			// Check API
			final ArgumentCaptor<VkBufferCreateInfo> captor = ArgumentCaptor.forClass(VkBufferCreateInfo.class);
			verify(lib).vkCreateBuffer(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

			// Check descriptor
			final VkBufferCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(IntegerEnumeration.mask(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT), info.usage);
			assertEquals(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, info.sharingMode);
			assertEquals(4, info.size);

			// Check internal memory allocation
			final var h = mem.handle(); // TODO
			verify(lib).vkGetBufferMemoryRequirements(eq(dev.handle()), isA(Pointer.class), isA(VkMemoryRequirements.class));
			verify(lib).vkBindBufferMemory(eq(dev.handle()), isA(Pointer.class), eq(h), eq(0L));
		}

		@Test
		void buildRequiresUsageFlags() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyBufferLength() {
			builder.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
//			when(allocation.size()).thenReturn(0L);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void staging() {
			buffer = VulkanBuffer.staging(dev, 4);
			assertNotNull(buffer);
			assertEquals(4, buffer.length());
		}
	}
}
