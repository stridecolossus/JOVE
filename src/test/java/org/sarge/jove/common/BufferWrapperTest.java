package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.BufferWrapper;
import org.sarge.jove.common.Bufferable;

public class BufferWrapperTest {
	private static final byte[] BYTES = {1, 2, 3};

	private BufferWrapper buffer;
	private ByteBuffer bb;
	private Bufferable data;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
		data = mock(Bufferable.class);
		buffer = new BufferWrapper(bb);
	}

	@Test
	void constructor() {
		assertEquals(bb, buffer.buffer());
	}

	@Test
	void allocate() {
		bb = BufferWrapper.allocate(3);
		assertNotNull(bb);
		assertEquals(3, bb.capacity());
		assertEquals(0, bb.position());
	}

	@Test
	void write() {
		BufferWrapper.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirect() {
		when(bb.isDirect()).thenReturn(true);
		BufferWrapper.write(BYTES, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}

	@Test
	void buffer() {
		bb = BufferWrapper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(BYTES.length, bb.capacity());
	}

	@Test
	void array() {
		bb = ByteBuffer.allocate(BYTES.length);
		bb.put(BYTES);
		buffer = new BufferWrapper(bb);
		assertArrayEquals(BYTES, buffer.array());
	}

	@Test
	void arrayDirect() {
		bb = BufferWrapper.buffer(BYTES);
		buffer = new BufferWrapper(bb);
		assertArrayEquals(BYTES, buffer.array());
	}

	@Test
	void rewind() {
		buffer.rewind();
		verify(bb).rewind();
	}

	@Test
	void append() {
		buffer.append(data);
		verify(data).buffer(bb);
	}

	@Test
	void insert() {
		when(data.length()).thenReturn(2);
		buffer.insert(3, data);
		verify(bb).position(2 * 3);
		verify(data).buffer(bb);
	}
}
