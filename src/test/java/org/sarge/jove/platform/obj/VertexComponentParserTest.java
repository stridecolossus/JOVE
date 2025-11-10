package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

@SuppressWarnings("resource")
public class VertexComponentParserTest {
	private Parser parser;
	private VertexComponentList<Point> list;

	@BeforeEach
	void before() {
		list = new VertexComponentList<>();
		parser = new VertexComponentParser<>(Point.SIZE, Point::new, list);
	}

	@Test
	void parse() {
		parser.parse(new Scanner("1 2 3"));
		assertEquals(List.of(new Point(1, 2, 3)), list);
	}

	@Test
	void invalid() {
		assertThrows(InputMismatchException.class, () -> parser.parse(new Scanner("cobblers 2 3")));
	}

	@Test
	void length() {
		assertThrows(NoSuchElementException.class, () -> parser.parse(new Scanner("1 2")));
	}
}
