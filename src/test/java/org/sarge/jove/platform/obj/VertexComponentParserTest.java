package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class VertexComponentParserTest {
	private Parser parser;
	private VertexComponentList<Point> list;

	@BeforeEach
	void before() {
		list = new VertexComponentList<>();
		parser = new VertexComponentParser<>(Point.SIZE, Point::new, list);
	}

	private void parse(String line) {
		parser.parse(line.split(" "));
	}

	@Test
	void integers() {
		parse("v 1 2 3");
		assertEquals(List.of(new Point(1, 2, 3)), list);
	}

	@Test
	void floats() {
		parse("v 1.0 2.0 3.0");
		assertEquals(List.of(new Point(1, 2, 3)), list);
	}

	@Test
	void invalid() {
		assertThrows(NumberFormatException.class, () -> parse("v cobblers 2 3"));
	}

	@Test
	void length() {
		assertThrows(IndexOutOfBoundsException.class, () -> parse("v"));
		assertThrows(IndexOutOfBoundsException.class, () -> parse("v 1 2"));
	}
}
