package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.io.BufferHelper;

public class BufferHelperTest {
	private static final byte[] BYTES = {1, 2, 3};

	private ByteBuffer bb;

	@BeforeEach
	void before() {
		bb = mock(ByteBuffer.class);
	}

	@Test
	void order() {
		assertEquals(ByteOrder.nativeOrder(), BufferHelper.ORDER);
	}

	@Test
	void allocate() {
		final ByteBuffer bb = BufferHelper.allocate(3);
		assertNotNull(bb);
		assertEquals(true, bb.isDirect());
		assertEquals(BufferHelper.ORDER, bb.order());
		assertEquals(0, bb.position());
		assertEquals(3, bb.capacity());
		assertEquals(3, bb.limit());
	}

	@Test
	void buffer() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		assertNotNull(bb);
		assertEquals(true, bb.isDirect());
		assertEquals(BufferHelper.ORDER, bb.order());
		assertEquals(3, bb.position());
		assertEquals(3, bb.capacity());
		assertEquals(3, bb.limit());
	}

	@Test
	void toArray() {
		final ByteBuffer bb = BufferHelper.buffer(BYTES);
		final byte[] array = BufferHelper.toArray(bb);
		assertArrayEquals(BYTES, array);
	}

	@Test
	void write() {
		BufferHelper.write(BYTES, bb);
		verify(bb).put(BYTES);
	}

	@Test
	void writeDirectBuffer() {
		when(bb.isDirect()).thenReturn(true);
		BufferHelper.write(BYTES, bb);
		for(byte b : BYTES) {
			verify(bb).put(b);
		}
	}

	@Test
	void wrapper() {
		class Wrapper<T extends Bufferable> implements Bufferable {
			private final Bufferable[] array;
			private int len;

			public Wrapper(int size, T obj) {
				array = new Bufferable[size];
				len = size * obj.length();
			}

			public void set(int index, T obj) {
				array[index] = notNull(obj);
			}

			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Bufferable b : array) {
					b.buffer(buffer);
				}
			}
		}

		Wrapper wrapper = new Wrapper(1, Matrix.IDENTITY);
		wrapper.set(0, Matrix.IDENTITY);

	}
}
