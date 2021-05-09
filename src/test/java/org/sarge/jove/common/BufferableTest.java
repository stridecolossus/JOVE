package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class BufferableTest {
	@Test
	void compound() {
		// Create a bufferable
		final Bufferable bufferable = mock(Bufferable.class);
		when(bufferable.length()).thenReturn(3);

		// Create a compound bufferable
		final Bufferable compound = Bufferable.of(bufferable, bufferable);
		assertEquals(2 * 3, compound.length());

		// Check buffering
		final ByteBuffer bb = mock(ByteBuffer.class);
		compound.buffer(bb);
		verify(bufferable, times(2)).buffer(bb);
	}
}
