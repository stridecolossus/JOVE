package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

@SuppressWarnings("resource")
public class FaceParserTest {
	private FaceParser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		parser = new FaceParser(model);
		model.positions().add(Point.ORIGIN);
		model.normals().add(Axis.X);
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
	}

	@Test
	void position() {
		parser.parse(new Scanner("1 1 1"));
		assertEquals(3, model.build().getFirst().count());
	}

	@Test
	void coordinate() {
		parser.parse(new Scanner("1/1 1/1 1/1"));
		assertEquals(3, model.build().getFirst().count());
	}

	@Test
	void normal() {
		parser.parse(new Scanner("1//1 1//1 1//1"));
		assertEquals(3, model.build().getFirst().count());
	}

	@Test
	void all() {
		parser.parse(new Scanner("1/1/1 1/1/1 1/1/1"));
		final var mesh = model.build().getFirst();
		assertEquals((3 + 3 + 2) * 4, mesh.vertices().length());
		assertEquals(3 * 4, mesh.index().length());
	}

	@Test
	void invalid() {
		assertThrows(NoSuchElementException.class, () -> parser.parse(new Scanner("")));
		assertThrows(NoSuchElementException.class, () -> parser.parse(new Scanner("1 1")));
	}

	@Test
	void triangle() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new Scanner("1/1/1/1 1 1")));
	}
}
