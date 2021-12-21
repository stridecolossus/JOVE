package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;

public class BufferWrapperTest {
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
