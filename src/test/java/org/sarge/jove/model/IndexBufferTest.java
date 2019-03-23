package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.IntBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.BufferObject.Mode;

public class IndexBufferTest {
	private static final List<Integer> INDICES = List.of(1, 2, 3);

	private IndexBufferObject index;
	private IntBuffer buffer;

	@BeforeEach
	public void before() {
		buffer = IntBuffer.allocate(3);
		index = new IndexBufferObject(Mode.DYNAMIC, buffer);
	}

	@Test
	public void constructor() {
		assertEquals(Mode.DYNAMIC, index.mode());
		assertEquals(1, index.size());
		assertEquals(3, index.length());
	}

	@Test
	public void update() {
		index.update(INDICES.stream().mapToInt(Integer::intValue));
		assertEquals(0, buffer.position());
		assertEquals(3, buffer.capacity());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
	}

	@Test
	public void push() {
		// TODO
		index.push();
		assertEquals(0, buffer.position());
	}
}
