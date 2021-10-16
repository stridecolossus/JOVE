package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BufferableTest {
	private static final byte[] BYTES = {1, 2, 3};

	private ByteBuffer bb;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
	}

	@Test
	void order() {
		assertEquals(ByteOrder.nativeOrder(), Bufferable.ORDER);
	}

	@Test
	void of() {
		// Create array wrapper
		final Bufferable bufferable = Bufferable.of(BYTES);
		assertNotNull(bufferable);
		assertEquals(BYTES.length, bufferable.length());

		// Check buffering
		bufferable.buffer(bb);
		verify(bb).put(BYTES);
	}

	@Test
	void toArray() {
		final int len = 42;
		final Bufferable obj = mock(Bufferable.class);
		when(obj.length()).thenReturn(len);

		final byte[] array = Bufferable.toArray(obj);
		assertNotNull(array);
		assertEquals(len, array.length);
		verify(obj).buffer(isA(ByteBuffer.class));
	}

	@Test
	void write() {
		Bufferable.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirectBuffer() {
		when(bb.isDirect()).thenReturn(true);
		Bufferable.write(BYTES, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}
}
