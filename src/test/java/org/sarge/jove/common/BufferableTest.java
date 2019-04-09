package org.sarge.jove.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BufferableTest {
	private Bufferable bufferable;

	@BeforeEach
	public void before() {
		bufferable = buffer -> buffer.put(42);
	}

	@Test
	public void populate() {
		final FloatBuffer buffer = mock(FloatBuffer.class);
		bufferable.buffer(buffer);
		verify(buffer).put(42f);
	}
}
