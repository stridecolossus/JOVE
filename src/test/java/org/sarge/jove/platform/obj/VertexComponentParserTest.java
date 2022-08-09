package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

public class VertexComponentParserTest {
	private Parser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		parser = new VertexComponentParser<>(3, Point::new, model.positions());
	}

	@Test
	void parse() {
		parser.parse(new String[]{"command", "1", "2", "3"}, model);
		assertEquals(List.of(new Point(1, 2, 3)), model.positions());
	}

	@Test
	void parseInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"command"}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"command", "2", "3", "4", "5"}, model));
	}
}
