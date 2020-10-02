package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.util.BufferFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VertexBufferTest extends AbstractVulkanTest {
	private VertexBuffer buffer;
	private Pointer mem;

	@BeforeEach
	void before() {
		mem = new Pointer(3);
		buffer = new VertexBuffer(new Pointer(1), dev, 2, mem);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(2, buffer.length());
	}

	@Test
	void bind() {
		final Handle handle = new Handle(new Pointer(4));
		final Command cmd = buffer.bind();
		assertNotNull(cmd);
		cmd.execute(lib, handle);
		verify(lib).vkCmdBindVertexBuffers(handle, 0, 1, new Handle[]{buffer.handle()}, new long[]{0});
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
		assertEquals(2, array[0].size);
	}

	@Test
	void load() {
		// Over-ride factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(lib.factory()).thenReturn(factory);

		// Mock vertex buffer memory
		final PointerByReference ref = mock(PointerByReference.class);
		final Pointer ptr = mock(Pointer.class);
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(factory.pointer()).thenReturn(ref);
		when(ref.getValue()).thenReturn(ptr);
		when(ptr.getByteBuffer(0, 1)).thenReturn(bb);

		// Load buffer
		final ByteBuffer src = BufferFactory.byteBuffer(1);
		buffer.load(src);

		// Check memory is mapped
		verify(lib).vkMapMemory(dev.handle(), mem, 0, 1, 0, ref);
		verify(lib).vkUnmapMemory(dev.handle(), mem);

		// Check buffer was copied to memory
		verify(bb).put(src);
	}

	@Test
	void loadBufferTooLarge() {
		final ByteBuffer src = BufferFactory.byteBuffer(999);
		assertThrows(IllegalStateException.class, () -> buffer.load(src));
	}

	@Test
	void destroy() {
		final Handle handle = buffer.handle();
		buffer.destroy();
		verify(lib).vkFreeMemory(dev.handle(), mem, null);
		verify(lib).vkDestroyBuffer(dev.handle(), handle, null);
	}

	@Test
	void build() {
		// TODO
	}
}
