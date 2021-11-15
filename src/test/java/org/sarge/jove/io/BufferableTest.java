package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.MockStructure;
import org.sarge.jove.io.Bufferable;

public class BufferableTest {
	private ByteBuffer bb;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
	}

	@Test
	void array() {
		final byte[] array = new byte[]{1, 2, 3};
		final Bufferable bufferable = Bufferable.of(array);
		assertNotNull(bufferable);
		assertEquals(array.length, bufferable.length());
		bufferable.buffer(bb);
		verify(bb).put(array);
	}

	@Test
	void structure() {
		final MockStructure struct = new MockStructure();
		final int len = struct.size();
		final Bufferable obj = Bufferable.of(struct);
		assertNotNull(obj);
		assertEquals(len, obj.length());
		obj.buffer(bb);
		verify(bb).put(struct.getPointer().getByteArray(0, len));
	}
}
