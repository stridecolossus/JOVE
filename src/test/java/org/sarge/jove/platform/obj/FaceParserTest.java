package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class FaceParserTest {
	private FaceParser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		parser = new FaceParser();
		model = new ObjectModel();
		model.positions().add(Point.ORIGIN);
		model.normals().add(Axis.X);
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
		model.start();
	}

	@Test
	void position() {
		parser.parse("1 1 1", model);
	}

	@Test
	void coordinate() {
		parser.parse("1/1 1/1 1/1", model);
	}

	@Test
	void normal() {
		parser.parse("1//1 1//1 1//1", model);
	}

	@Test
	void all() {
		parser.parse("1/1/1 1/1/1 1/1/1", model);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("", model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 1", model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 1 1 1", model));
	}

	@Test
	void length() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1/1/1/1 1 2", model));
	}
}
