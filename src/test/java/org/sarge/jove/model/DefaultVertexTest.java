package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex.DefaultVertex;

class DefaultVertexTest {
	private DefaultVertex vertex;
	private Point pos;

	@BeforeEach
	void before() {
		pos = new Point(1, 2, 3);
		vertex = new DefaultVertex(pos, Colour.WHITE);
	}

	@Test
	void position() {
		assertEquals(pos, vertex.position());
	}

//	@Test
//	void layout() {
//		assertEquals(new Layout(Point.LAYOUT, Colour.LAYOUT), vertex.layout());
//	}

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

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new DefaultVertex(pos, Colour.WHITE));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new DefaultVertex(pos));
	}
}
