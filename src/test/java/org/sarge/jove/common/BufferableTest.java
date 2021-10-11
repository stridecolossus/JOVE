package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class BufferableTest {
	@Test
	void array() {
		// Create array wrapper
		final byte[] bytes = new byte[]{1, 2, 3};
		final Bufferable bufferable = Bufferable.of(bytes);
		assertNotNull(bufferable);
		assertEquals(bytes.length, bufferable.length());

		// Check buffering
		final ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bufferable.buffer(bb);
		assertArrayEquals(bytes, bb.array());
	}
}
