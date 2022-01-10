package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

	@SuppressWarnings("static-method")
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
	void clear() {
		list.add(1);
		list.clear();
		assertEquals(0, list.size());
		assertEquals(0, list.stream().count());
		assertEquals(CAPACITY, list.capacity());
	}

	@Test
	void bufferInteger() {
		// Buffer list
		final IntBuffer buffer = mock(IntBuffer.class);
		list.add(3);
		list.buffer(buffer);

		// Check array
		final ArgumentCaptor<int[]> captor = ArgumentCaptor.forClass(int[].class);
		verify(buffer).put(captor.capture(), eq(0), eq(1));

		// Check array started with the expected value
		final int[] array = captor.getValue();
		assertNotNull(array);
		assertTrue(array.length >= 1);
		assertEquals(3, array[0]);
	}

	@Test
	void bufferIntegerDirect() {
		final IntBuffer buffer = mock(IntBuffer.class);
		when(buffer.isDirect()).thenReturn(true);
		list.add(3);
		list.buffer(buffer);
		verify(buffer).put(3);
	}

	@Test
	void bufferShort() {
		final ShortBuffer buffer = mock(ShortBuffer.class);
		list.add(3);
		list.buffer(buffer);
		verify(buffer).put(new short[]{3}, 0, 1);
	}

	@Test
	void bufferShortDirect() {
		final ShortBuffer buffer = mock(ShortBuffer.class);
		when(buffer.isDirect()).thenReturn(true);
		list.add(3);
		list.buffer(buffer);
		verify(buffer).put((short) 3);
	}
}
