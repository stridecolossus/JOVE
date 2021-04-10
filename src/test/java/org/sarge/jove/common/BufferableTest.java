package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class BufferableTest {
	@Test
	void of() {
		// Wrap array as a bufferable
		final byte[] array = {1, 2, 3};
		final Bufferable bufferable = Bufferable.of(array);
		assertNotNull(bufferable);
		assertEquals(3, bufferable.length());
		assertSame(array, bufferable.toByteArray());

		// Convert back to buffer
		final ByteBuffer bb = ByteBuffer.allocate(3);
		bufferable.buffer(bb);
		assertEquals(0, bb.remaining());

		// Check contents
		bb.flip();
		assertEquals(1, bb.get());
		assertEquals(2, bb.get());
		assertEquals(3, bb.get());
	}
}
