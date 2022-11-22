package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

class MutableVertexTest {
	private MutableVertex vertex;

	@BeforeEach
	void before() {
		vertex = new MutableVertex();
	}

	@Test
	void empty() {
		assertEquals(new CompoundLayout(), vertex.layout());
	}

	@Test
	void position() {
		vertex.position(Point.ORIGIN);
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		vertex.normal(Axis.Y);
		assertEquals(Axis.Y, vertex.normal());
	}

	@Test
	void coordinate() {
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		assertEquals(Coordinate2D.BOTTOM_LEFT, vertex.coordinate());
	}

	@Test
	void colour() {
		vertex.colour(Colour.WHITE);
		assertEquals(Colour.WHITE, vertex.colour());
	}

	@Test
	void layout() {
		vertex.position(Point.ORIGIN);
		vertex.normal(Axis.Y);
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		vertex.colour(Colour.WHITE);
		assertEquals(new CompoundLayout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT, Colour.LAYOUT), vertex.layout());
	}

	@Test
	void buffer() {
		vertex.position(Point.ORIGIN);
		vertex.normal(Axis.Y);
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		vertex.colour(Colour.WHITE);
		final ByteBuffer bb = mock(ByteBuffer.class);
		vertex.buffer(bb);
		verify(bb, times(3 + 3 + 2 + 4)).putFloat(anyFloat());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new MutableVertex());
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, mock(Vertex.class));
	}
}
