package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.util.MockStructure;

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
	void bufferable() {
		final Bufferable bufferable = BufferHelper.of(BYTES);
		assertNotNull(bufferable);
		assertEquals(3, bufferable.length());
		bufferable.buffer(bb);
		verify(bb).put(BYTES);
	}

	@Test
	void direct() {
		final Bufferable bufferable = BufferHelper.of(BYTES);
		when(bb.isDirect()).thenReturn(true);
		bufferable.buffer(bb);
		verify(bb).put((byte) 1);
		verify(bb).put((byte) 2);
		verify(bb).put((byte) 3);
	}

	@Test
	void structure() {
		final MockStructure struct = new MockStructure();
		final int len = struct.size();
		final Bufferable obj = BufferHelper.of(struct);
		assertNotNull(obj);
		assertEquals(len, obj.length());
		obj.buffer(bb);
		verify(bb).put(struct.getPointer().getByteArray(0, len));
	}

	@Test
	void buffer() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(BYTES.length, bb.capacity());
	}

	@Test
	void array() {
		final ByteBuffer bb = ByteBuffer.allocate(3).put(BYTES);
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
