package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
		assertEquals(true, buffer.isDirect());
	}

	@Test
	void of() {
		// Create buffers
		final ByteBuffer src = ByteBuffer.allocate(42);
		final ByteBuffer dest = mock(ByteBuffer.class);

		// Wrap source as bufferable
		final Bufferable obj = Bufferable.of(src);
		assertEquals(42, obj.length());

		// Check buffer operation
		obj.buffer(dest);
		verify(dest).put(src);
	}
}
