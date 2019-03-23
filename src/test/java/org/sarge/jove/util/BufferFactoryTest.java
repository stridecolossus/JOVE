package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.jupiter.api.Test;

public class BufferFactoryTest {
	@Test
	public void byteBuffer() {
		final ByteBuffer buffer = BufferFactory.byteBuffer(2);
		assertNotNull(buffer);
		assertEquals(2, buffer.capacity());
		assertEquals(0, buffer.position());
		assertEquals(ByteOrder.nativeOrder(), buffer.order());
	}

	@Test
	public void floatBuffer() {
		final FloatBuffer buffer = BufferFactory.floatBuffer(3);
		assertNotNull(buffer);
		assertEquals(3, buffer.capacity());
		assertEquals(0, buffer.position());
		assertEquals(ByteOrder.nativeOrder(), buffer.order());
	}

	@Test
	public void intBuffer() {
		final IntBuffer buffer = BufferFactory.intBuffer(4);
		assertNotNull(buffer);
		assertEquals(4, buffer.capacity());
		assertEquals(0, buffer.position());
		assertEquals(ByteOrder.nativeOrder(), buffer.order());
	}
}
