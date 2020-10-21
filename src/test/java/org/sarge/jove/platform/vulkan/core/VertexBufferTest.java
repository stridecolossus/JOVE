package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
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
		final Command cmd = buffer.bind();
		assertNotNull(cmd);
		cmd.execute(lib, handle);
		verify(lib).vkCmdBindVertexBuffers(eq(handle), eq(0), eq(1), isA(Pointer.class), eq(new long[]{0}));
	}

	@Test
	void bindIndexBuffer() {
		final Handle handle = new Handle(new Pointer(5));
		final Command cmd = buffer.index();
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
		buffer.load(ByteBuffer.allocate(3));

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
		final ByteBuffer obj = ByteBuffer.allocate(999);
		assertThrows(IllegalStateException.class, () -> buffer.load(obj));
	}

	@Test
	void loadInvalidOffset() {
		final ByteBuffer obj = ByteBuffer.allocate(1);
		assertThrows(IllegalStateException.class, () -> buffer.load(obj, 3));
	}

	@Test
	void destroy() {
		final Handle handle = buffer.handle();
		buffer.destroy();
		verify(lib).vkFreeMemory(dev.handle(), mem, null);
		verify(lib).vkDestroyBuffer(dev.handle(), handle, null);
	}

	@Nested
	class BuilderTests {
		private VertexBuffer.Builder builder;

		@BeforeEach
		void before() {
			final Pointer mem = new Pointer(3);
			when(dev.allocate(isA(VkMemoryRequirements.class), anySet())).thenReturn(mem);
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
			verify(lib).vkCreateBuffer(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

			// Check descriptor
			final VkBufferCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(IntegerEnumeration.mask(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT), info.usage);
			assertEquals(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, info.sharingMode);
			assertEquals(4, info.size);

			// Check internal memory allocation
			verify(lib).vkGetBufferMemoryRequirements(eq(dev.handle()), eq(factory.ptr.getValue()), isA(VkMemoryRequirements.class));
			verify(lib).vkBindBufferMemory(dev.handle(), factory.ptr.getValue(), mem, 0L);
		}

		@Test
		void buildRequiresUsageFlags() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyBufferLength() {
			builder.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void staging() {
			buffer = VertexBuffer.staging(dev, 42);
			assertNotNull(buffer);
			assertEquals(42, buffer.length());
		}
	}
}
