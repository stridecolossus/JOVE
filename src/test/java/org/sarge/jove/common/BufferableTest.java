package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

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
	void of() {
		// Create array wrapper
		final Bufferable bufferable = Bufferable.of(BYTES);
		assertNotNull(bufferable);
		assertEquals(BYTES.length, bufferable.length());

		// Check buffering
		bufferable.buffer(bb);
		verify(bb).put(BYTES);
	}
}
