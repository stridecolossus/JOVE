package org.sarge.jove.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class BufferableTest {
	@Test
	void write() throws IOException {
		// Create bufferable object
		final Bufferable obj = new Bufferable() {
			@Override
			public long length() {
				return 1;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				buffer.put((byte) 42);
			}
		};

		// Write object
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		Bufferable.write(obj, out);
		assertEquals(1, out.size());
		assertArrayEquals(new byte[]{(byte) 42}, out.toByteArray());
	}

	@Test
	void read() {
		// Read bufferable from input stream
		final Bufferable obj = Bufferable.read(new ByteArrayInputStream(new byte[]{(byte) 42}));
		assertNotNull(obj);
		assertEquals(1, obj.length());

		// Check buffer
		final ByteBuffer bb = ByteBuffer.allocate(1);
		obj.buffer(bb);
		bb.flip();
		assertArrayEquals(new byte[]{(byte) 42}, bb.array());
	}
}
