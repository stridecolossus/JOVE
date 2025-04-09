package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.FloatArrayParser;

public class VertexComponentParserTest {
	private Parser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		parser = new VertexComponentParser<>(new FloatArrayParser<>(Point.SIZE, Point::new), ObjectModel::positions);
	}

	@Test
	void parse() {
		parser.parse("1 2 3", model);
		assertEquals(List.of(new Point(1, 2, 3)), model.positions());
	}

	@Test
	void parseInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("", model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2", model));
	}
}
