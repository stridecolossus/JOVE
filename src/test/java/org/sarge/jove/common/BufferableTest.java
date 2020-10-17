package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class BufferableTest {
	@Test
	void allocate() {
		final ByteBuffer buffer = Bufferable.allocate(new byte[]{42});
		assertNotNull(buffer);
		assertEquals(1, buffer.capacity());
		assertEquals(1, buffer.limit());
		assertEquals(0, buffer.position());
		assertEquals(42, buffer.get());
	}
}
