package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ByteSource.Sink;

public class ByteSourceTest {
	private byte[] array;
	private Sink sink;

	@BeforeEach
	void before() {
		array = new byte[1];
		sink = mock(Sink.class);
	}

	@Nested
	class ArrayTests {
		private ByteSource src;

		@BeforeEach
		void before() {
			src = ByteSource.of(array);
		}

		@Test
		void constructor() {
			assertNotNull(src);
			assertEquals(array, src.toByteArray());
		}

		@Test
		void write() {
			src.write(sink);
			verify(sink).write(array);
		}
	}

	@Nested
	class BufferTests {
		private ByteBuffer buffer;
		private ByteSource src;

		@BeforeEach
		void before() {
			buffer = ByteBuffer.wrap(array);
			src = ByteSource.of(buffer);
		}

		@Test
		void constructor() {
			assertNotNull(src);
			assertEquals(array, src.toByteArray());
		}

		@Test
		void write() {
			src.write(sink);
			verify(sink).write(buffer);
		}
	}
}
