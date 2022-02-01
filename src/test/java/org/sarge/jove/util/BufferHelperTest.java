package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;

@SuppressWarnings("static-method")
class BufferHelperTest {
	private static final byte[] BYTES = {1, 2, 3};

	@Test
	void allocate() {
		final ByteBuffer bb = BufferHelper.allocate(3);
		assertNotNull(bb);
		assertEquals(3, bb.capacity());
		assertEquals(0, bb.position());
		assertEquals(true, bb.isDirect());
		assertEquals(ByteOrder.LITTLE_ENDIAN, bb.order());
	}

	@Test
	void write() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		BufferHelper.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirect() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.isDirect()).thenReturn(true);
		BufferHelper.write(BYTES, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}

	@Test
	void buffer() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(BYTES.length, bb.capacity());
	}

	@Test
	void array() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertArrayEquals(BYTES, BufferHelper.array(bb));
	}

	@Test
	void arrayDirect() {
		final ByteBuffer bb = ByteBuffer.allocate(3).put(BYTES);
		assertArrayEquals(BYTES, BufferHelper.array(bb));
	}

	@Test
	void insert() {
		// Create a bufferable object to insert
		final Bufferable obj = mock(Bufferable.class);
		when(obj.length()).thenReturn(3);

		// Insert and check buffer updated
		final ByteBuffer bb = mock(ByteBuffer.class);
		BufferHelper.insert(2, obj, bb);
		verify(bb).position(2 * 3);
		verify(obj).buffer(bb);
	}
}
