package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class ByteSizedBufferableTest {
	@Test
	void of() {
		final byte[] array = new byte[3];
		final ByteSizedBufferable bufferable = ByteSizedBufferable.of(array);
		assertEquals(3, bufferable.length());

		final ByteBuffer bb = ByteBuffer.allocate(3);
		bufferable.buffer(bb);
		assertEquals(0, bb.remaining());
	}
}
