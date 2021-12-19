package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;

public class IntegerListTest {
	private static final int CAPACITY = 5;

	private IntegerList list;

	@BeforeEach
	void before() {
		list = new IntegerList(CAPACITY, 1);
	}

	@Test
	void constructor() {
		assertEquals(0, list.size());
		assertEquals(CAPACITY, list.capacity());
		assertNotNull(list.stream());
		assertEquals(0, list.stream().count());
	}

	@Test
	void constructorInvalidGrowthFactor() {
		assertThrows(IllegalArgumentException.class, () -> new IntegerList(1, 0));
		assertThrows(IllegalArgumentException.class, () -> new IntegerList(1, -1));
	}

	@Test
	void add() {
		list.add(3);
		assertEquals(1, list.size());
		assertEquals(CAPACITY, list.capacity());
		assertArrayEquals(new int[]{3}, list.stream().toArray());
	}

	@Test
	void slice() {
		list.add(1);
		list.add(2);
		list.add(3);
		final int[] slice = new int[2];
		list.slice(1, slice);
		assertArrayEquals(new int[]{2, 3}, slice);
	}

	@Test
	void sliceInvalidLength() {
		list.add(1);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> list.slice(0, new int[2]));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> list.slice(1, new int[1]));
	}

	@Test
	void growth() {
		list = new IntegerList(2, 3);
		list.add(1);
		list.add(2);
		list.add(3);
		assertEquals(3, list.size());
		assertEquals(2 + (2 * 3), list.capacity());
		assertArrayEquals(new int[]{1, 2, 3}, list.stream().toArray());
	}

	@Test
	void bufferable() {
		// Create bufferable wrapper
		final Bufferable obj = list.bufferable();
		assertNotNull(obj);

		// Add some values
		list.add(1);
		list.add(2);
		assertEquals(2 * Integer.BYTES, obj.length());

		// Check can be buffered
		final ByteBuffer bb = mock(ByteBuffer.class);
		final IntBuffer buffer = mock(IntBuffer.class);
		when(bb.asIntBuffer()).thenReturn(buffer);
		obj.buffer(bb);
		verify(buffer).put(new int[]{1, 2, 0, 0, 0}, 0, 2);

		// Check direct buffers
		when(buffer.isDirect()).thenReturn(true);
		when(buffer.put(anyInt())).thenReturn(buffer);
		obj.buffer(bb);
		verify(buffer).put(1);
		verify(buffer).put(2);
	}

	@Test
	void clear() {
		list.add(1);
		list.clear();
		assertEquals(0, list.size());
		assertEquals(0, list.stream().count());
		assertEquals(CAPACITY, list.capacity());
	}
}
