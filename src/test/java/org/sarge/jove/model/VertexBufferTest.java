package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.BufferObject.Mode;
import org.sarge.jove.model.VertexBufferObject.Attribute;

public class VertexBufferTest {
	private VertexBufferObject vbo;
	private Attribute attr;
	private FloatBuffer buffer;

	@BeforeEach
	public void before() {
		attr = new Attribute(Point.SIZE);
		buffer = FloatBuffer.allocate(Point.SIZE);
		vbo = new VertexBufferObject(List.of(attr), Mode.DYNAMIC, buffer);
	}

	@Test
	public void attribute() {
		assertEquals(Point.SIZE, attr.size());
	}

	@Test
	public void constructor() {
		assertEquals(List.of(attr), vbo.layout());
		assertEquals(Mode.DYNAMIC, vbo.mode());
		assertEquals(Point.SIZE, vbo.size());
		assertEquals(Point.SIZE, vbo.length());
	}

	@Test
	public void emptyAttributes() {
		assertThrows(IllegalArgumentException.class, () -> new VertexBufferObject(List.of(), Mode.STATIC, buffer));
	}

	@Test
	public void update() {
		final Point pt = new Point(1, 2, 3);
		vbo.update(Stream.of(pt));
		assertEquals(0, buffer.position());
		assertFloatArrayEquals(pt.toArray(), buffer.array());
	}

	@Test
	public void push() {
		// TODO
		assertEquals(0, buffer.position());
	}
}
