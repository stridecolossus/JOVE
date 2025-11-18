package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(new Point(1, 2, 3));
	}

	@Nested
	class Simple {
    	@Test
    	void buffer() {
    		final var buffer = ByteBuffer.allocate(3 * Float.BYTES);
    		vertex.buffer(buffer);
    		buffer.rewind();
    		assertEquals(1f, buffer.getFloat());
    		assertEquals(2f, buffer.getFloat());
    		assertEquals(3f, buffer.getFloat());
    	}

    	@Test
    	void equals() {
    		assertEquals(vertex, vertex);
    		assertEquals(vertex, new Vertex(new Point(1, 2, 3)));
    		assertNotEquals(vertex, null);
    		assertNotEquals(vertex, new Vertex(Point.ORIGIN));
    	}
    }

	@Nested
	class Compound {
		@BeforeEach
		void before() {
    		vertex.normal(Axis.X);
    		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		}

    	@Test
    	void buffer() {
    		final var buffer = ByteBuffer.allocate((3 + 3 + 2) * Float.BYTES);
    		vertex.buffer(buffer);
    		buffer.rewind();
    		assertEquals(1f, buffer.getFloat());
    		assertEquals(2f, buffer.getFloat());
    		assertEquals(3f, buffer.getFloat());
    		assertEquals(1f, buffer.getFloat());
    		assertEquals(0f, buffer.getFloat());
    		assertEquals(0f, buffer.getFloat());
    		assertEquals(0f, buffer.getFloat());
    		assertEquals(1f, buffer.getFloat());
    	}

    	@Test
    	void equals() {
    		assertEquals(vertex, vertex);
    		assertNotEquals(vertex, null);
    		assertNotEquals(vertex, new Vertex(Point.ORIGIN));
    	}
    }
}
