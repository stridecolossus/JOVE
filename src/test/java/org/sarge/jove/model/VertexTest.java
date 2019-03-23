package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.texture.TextureCoordinate;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	public void before() {
		vertex = new Vertex(Point.ORIGIN);
	}

	@Test
	public void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(null, vertex.normal());
		assertEquals(null, vertex.coords());
		assertEquals(null, vertex.colour());
	}

	@Test
	public void converter() {
		assertEquals(List.of(Component.POSITION, Component.NORMAL, Component.TEXTURE_COORDINATE, Component.COLOUR), Component.CONVERTER.apply("VNTC"));
	}

	@Test
	public void normal() {
		vertex = vertex.normal(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, vertex.normal());
		assertEquals(true, vertex.matches(Set.of(Component.NORMAL)));
		assertEquals(Vector.X_AXIS, vertex.map(Component.NORMAL));
	}

	@Test
	public void coords() {
		final TextureCoordinate coords = TextureCoordinate.of(1, 2);
		vertex = vertex.coords(coords);
		assertEquals(coords, vertex.coords());
		assertEquals(true, vertex.matches(Set.of(Component.TEXTURE_COORDINATE)));
		assertEquals(coords, vertex.map(Component.TEXTURE_COORDINATE));
	}

	@Test
	public void colour() {
		vertex = vertex.colour(Colour.WHITE);
		assertEquals(Colour.WHITE, vertex.colour());
		assertEquals(true, vertex.matches(Set.of(Component.COLOUR)));
		assertEquals(Colour.WHITE, vertex.map(Component.COLOUR));
	}

	@Test
	public void matches() {
		assertEquals(true, vertex.matches(Set.of(Component.POSITION)));
		assertEquals(false, vertex.matches(Set.of(Component.NORMAL)));
		assertEquals(false, vertex.matches(Set.of(Component.TEXTURE_COORDINATE)));
		assertEquals(false, vertex.matches(Set.of(Component.COLOUR)));
	}

	@Test
	public void map() {
		assertEquals(vertex.position(), vertex.map(Component.POSITION));
		assertEquals(null, vertex.map(Component.NORMAL));
		assertEquals(null, vertex.map(Component.TEXTURE_COORDINATE));
		assertEquals(null, vertex.map(Component.COLOUR));
	}

	@Test
	public void equals() {
		assertTrue(vertex.equals(vertex));
		assertFalse(vertex.equals(null));
		assertFalse(vertex.equals(new Vertex(new Point(1, 2, 3))));
	}
}
