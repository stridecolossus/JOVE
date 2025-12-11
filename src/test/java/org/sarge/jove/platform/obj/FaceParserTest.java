package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Vertex;

class FaceParserTest {
	private FaceParser parser;
	private ObjectModel model;
	private List<Vertex> vertices;

	@BeforeEach
	void before() {
		vertices = new ArrayList<>();
		model = new ObjectModel() {
			@Override
			public void add(Vertex vertex) {
				super.add(vertex);
				vertices.add(vertex);
			}
		};
		parser = new FaceParser(model);
		model.positions().add(Point.ORIGIN);
		model.normals().add(Axis.X);
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
	}

	private void parse(String line, Bufferable... components) {
		final Vertex vertex = new Vertex(components);
		final var expected = Collections.nCopies(3, vertex);
		parser.parse(line.split(" "));
		assertEquals(expected, vertices);
	}

	@Test
	void position() {
		parse("f 1 1 1", Point.ORIGIN);
	}

	@Test
	void coordinate() {
		parse("f 1/1 1/1 1/1", Point.ORIGIN, Coordinate2D.BOTTOM_LEFT);
	}

	@Test
	void normal() {
		parse("f 1//1 1//1 1//1", Point.ORIGIN, Axis.X);
	}

	@Test
	void all() {
		parse("f 1/1/1 1/1/1 1/1/1", Point.ORIGIN, Axis.X, Coordinate2D.BOTTOM_LEFT);
	}

	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> parse(""));
		assertThrows(IndexOutOfBoundsException.class, () -> parse("f "));
		assertThrows(IndexOutOfBoundsException.class, () -> parse("f 1 1"));
	}

	@Test
	void triangle() {
		assertThrows(IllegalArgumentException.class, () -> parse("f 1/1/1/1 1 1"));
	}
}
