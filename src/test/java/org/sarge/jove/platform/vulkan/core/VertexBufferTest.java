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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.Memory;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VertexBufferTest extends AbstractVulkanTest {
	private VertexBuffer buffer;
	private Pointer mem;

	@BeforeEach
	void before() {
		mem = new Pointer(3);
		buffer = new VertexBuffer(new Pointer(1), dev, 3, mem);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(3, buffer.length());
	}

	@Test
	void bind() {
		final Handle handle = new Handle(new Pointer(4));
		final Command cmd = buffer.bindVertexBuffer();
		assertNotNull(cmd);
		cmd.execute(lib, handle);
		verify(lib).vkCmdBindVertexBuffers(eq(handle), eq(0), eq(1), isA(Pointer.class), eq(new long[]{0}));
	}

	@Test
	void bindIndexBuffer() {
		final Handle handle = new Handle(new Pointer(5));
		final Command cmd = buffer.bindIndexBuffer();
		assertNotNull(cmd);
		cmd.execute(lib, handle);
		verify(lib).vkCmdBindIndexBuffer(handle, buffer.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
	}

	@Test
	void copy() {
		// Create a destination buffer
		final VertexBuffer dest = mock(VertexBuffer.class);
		final Handle destHandle = new Handle(new Pointer(5));
		when(dest.handle()).thenReturn(destHandle);
		when(dest.length()).thenReturn(buffer.length());

		// Execute copy command
		final Handle handle = new Handle(new Pointer(6));
		final Command cmd = buffer.copy(dest);
		assertNotNull(cmd);
		cmd.execute(lib, handle);

		// Check API
		final ArgumentCaptor<VkBufferCopy[]> captor = ArgumentCaptor.forClass(VkBufferCopy[].class);
		verify(lib).vkCmdCopyBuffer(eq(handle), eq(buffer.handle()), eq(destHandle), eq(1), captor.capture());

		// Check region
		final VkBufferCopy[] array = captor.getValue();
		assertNotNull(array);
		assertEquals(1, array.length);
		assertEquals(3, array[0].size);
	}

	@Test
	void copyTooSmall() {
		final VertexBuffer dest = mock(VertexBuffer.class);
		assertThrows(IllegalStateException.class, "too small", () -> buffer.copy(dest));
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

		// Init internal buffer
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(data.getByteBuffer(0, 3)).thenReturn(bb);

		// Load buffer
		buffer.load(Bufferable.of(ByteBuffer.allocate(3)));

		// Check memory is mapped
		verify(lib).vkMapMemory(dev.handle(), mem, 0, 3L, 0, ref);
		verify(lib).vkUnmapMemory(dev.handle(), mem);

		// Load bufferable at offset
		final Bufferable obj = mock(Bufferable.class);
		when(data.getByteBuffer(0, 1)).thenReturn(bb);
		buffer.load(obj, 1, 2);
		verify(obj).buffer(bb);
	}

	@Test
	void loadBufferTooLarge() {
		final ByteBuffer bb = ByteBuffer.allocate(999);
		assertThrows(IllegalStateException.class, () -> buffer.load(Bufferable.of(bb)));
	}

	@Test
	void loadInvalidOffset() {
		final ByteBuffer bb = ByteBuffer.allocate(1);
		assertThrows(IllegalStateException.class, () -> buffer.load(Bufferable.of(bb), 3));
	}

	@Test
	void destroy() {
		final Handle handle = buffer.handle();
		buffer.destroy();
		verify(lib).vkFreeMemory(dev.handle(), mem, null);
		verify(lib).vkDestroyBuffer(dev.handle(), handle, null);
	}

	@Nested
	class UniformBufferResourceTests {
		@Test
		void constructor() {
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, buffer.type());
		}

		@Test
		void populate() {
			final var write = new VkWriteDescriptorSet();
			buffer.populate(write);

			final VkDescriptorBufferInfo info = write.pBufferInfo;
			assertNotNull(info);
			assertEquals(buffer.handle(), info.buffer);
			assertEquals(buffer.length(), info.range);
			assertEquals(0, info.offset);
		}
	}

	@Nested
	class BuilderTests {
		private VertexBuffer.Builder builder;
		private MemoryAllocator.Request allocation;

		@BeforeEach
		void before() {
			// Init VBO memory allocation
			// Init image memory
			final Pointer ptr = new Pointer(3);
			final Memory mem = mock(Memory.class);
			when(mem.memory()).thenReturn(ptr);
			final MemoryAllocator allocator = mock(MemoryAllocator.class);
			allocation = mock(MemoryAllocator.Request.class);
			when(dev.allocator()).thenReturn(allocator);
			when(allocator.request()).thenReturn(allocation);
			when(allocation.allocate()).thenReturn(mem);
			when(allocation.init(any())).thenReturn(allocation);
			//when(allocation.size()).thenReturn(4L);

			// Create builder
			builder = new VertexBuffer.Builder(dev);
		}

		@Test
		void build() {
			// Build buffer
			buffer = builder
					.length(4)
					.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
					.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
					.mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE)
					.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
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
			verify(lib).vkGetBufferMemoryRequirements(eq(dev.handle()), isA(Pointer.class), isA(VkMemoryRequirements.class));
			verify(lib).vkBindBufferMemory(eq(dev.handle()), isA(Pointer.class), eq(mem), eq(0L));
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
			buffer = VertexBuffer.staging(dev, 4);
			assertNotNull(buffer);
			assertEquals(4, buffer.length());
		}
	}
}
