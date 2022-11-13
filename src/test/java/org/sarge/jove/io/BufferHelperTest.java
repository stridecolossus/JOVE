package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.*;

import org.junit.jupiter.api.*;

class BufferHelperTest {
	private static final byte[] BYTES = {1, 2, 3};

	private ByteBuffer bb;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
	}

	@Test
	void allocate() {
		final ByteBuffer buffer = BufferHelper.allocate(3);
		assertNotNull(buffer);
		assertEquals(3, buffer.capacity());
		assertEquals(0, buffer.position());
		assertEquals(true, buffer.isDirect());
		assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
	}

	@Test
	void write() {
		BufferHelper.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirect() {
		when(bb.isDirect()).thenReturn(true);
		BufferHelper.write(BYTES, bb);
		verify(bb).put((byte) 1);
		verify(bb).put((byte) 2);
		verify(bb).put((byte) 3);
	}

	@Test
	void buffer() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(BYTES.length, bb.capacity());
	}

	@Test
	void array() {
		bb = ByteBuffer.allocate(3).put(BYTES);
		assertArrayEquals(BYTES, BufferHelper.array(bb));
	}

	@Test
	void arrayDirect() {
		bb = ByteBuffer.allocateDirect(3).put(BYTES);
		assertArrayEquals(BYTES, BufferHelper.array(bb));
	}
}
