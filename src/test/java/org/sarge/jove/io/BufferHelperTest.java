package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BufferHelperTest {
	private static final byte[] BYTES = {1, 2, 3};

	private ByteBuffer bb;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
	}

	@Test
	void order() {
		assertEquals(ByteOrder.nativeOrder(), BufferHelper.ORDER);
	}

	@Test
	void allocate() {
		final ByteBuffer bb = BufferHelper.allocate(3);
		assertNotNull(bb);
		assertEquals(true, bb.isDirect());
		assertEquals(BufferHelper.ORDER, bb.order());
		assertEquals(0, bb.position());
		assertEquals(3, bb.capacity());
		assertEquals(3, bb.limit());
	}

	@Test
	void buffer() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(true, bb.isDirect());
		assertEquals(BufferHelper.ORDER, bb.order());
		assertEquals(3, bb.position());
		assertEquals(3, bb.capacity());
		assertEquals(3, bb.limit());
	}

	@Test
	void toArray() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		final byte[] array = BufferHelper.toArray(bb);
		assertArrayEquals(BYTES, array);
	}

	@Test
	void write() {
		BufferHelper.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirectBuffer() {
		when(bb.isDirect()).thenReturn(true);
		BufferHelper.write(BYTES, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}

	@Test
	void copy() {
		final ByteBuffer src = BufferHelper.buffer(BYTES);
		BufferHelper.copy(src, bb);
		verify(bb).put(src);
	}

	@Test
	void copyDirect() {
		final ByteBuffer src = BufferHelper.buffer(BYTES);
		src.rewind();
		when(bb.isDirect()).thenReturn(true);
		BufferHelper.copy(src, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}
}
