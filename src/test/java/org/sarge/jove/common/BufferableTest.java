package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class BufferableTest {
	@Test
	void toByteArray() {
		// Create a bufferable
		final Bufferable bufferable = new Bufferable() {
			@Override
			public int length() {
				return 2;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				buffer.put((byte) 2);
				buffer.put((byte) 3);
			}
		};

		// Convert to array
		final byte[] array = bufferable.toByteArray();
		assertArrayEquals(new byte[]{2, 3}, array);
	}

	@Test
	void array() {
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
