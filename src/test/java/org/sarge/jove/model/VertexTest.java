package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;

public class VertexTest {
	private Vertex vertex;
	private Point pos;

	@BeforeEach
	void before() {
		pos = new Point(1, 2, 3);
		vertex = new Vertex(List.of(pos, Colour.WHITE));
	}

	@Test
	void of() {
		assertEquals(vertex, Vertex.of(pos, Colour.WHITE));
	}

	@Test
	void components() {
		assertEquals(pos, vertex.component(0));
		assertEquals(Colour.WHITE, vertex.component(1));
	}

	@Test
	void length() {
		assertEquals((3 + 4) * Float.BYTES, vertex.length());
	}

	@Test
	void buffer() {
		final int len = (3 + 4) * Float.BYTES;
		final ByteBuffer bb = ByteBuffer.allocate(len);
		vertex.buffer(bb);
		bb.rewind();
		assertEquals(pos.x, bb.getFloat());
		assertEquals(pos.y, bb.getFloat());
		assertEquals(pos.z, bb.getFloat());
		assertEquals(1, bb.getFloat());
		assertEquals(1, bb.getFloat());
		assertEquals(1, bb.getFloat());
		assertEquals(1, bb.getFloat());
		assertEquals(0, bb.remaining());
	}
}
