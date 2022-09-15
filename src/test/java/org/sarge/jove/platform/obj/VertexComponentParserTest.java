package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.FloatArrayConverter;

public class VertexComponentParserTest {
	private Parser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		parser = new VertexComponentParser<>(new FloatArrayConverter<>(Point.SIZE, Point::new), model.positions());
	}

	@Test
	void parse() {
		parser.parse("1 2 3", model);
		assertEquals(List.of(new Point(1, 2, 3)), model.positions());
	}

	@Test
	void parseInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(StringUtils.EMPTY, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2", model));
	}
}
