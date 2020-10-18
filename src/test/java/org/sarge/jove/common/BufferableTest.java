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
	}

	@Test
	void of() {
		final ByteBuffer src = mock(ByteBuffer.class);
		final ByteBuffer dest = mock(ByteBuffer.class);
		final Bufferable obj = Bufferable.of(src);
		obj.buffer(dest);
		verify(dest).put(src);
	}
}
