package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class VertexComponentParserTest {
	private Parser parser;
	private ObjectModel model;
	private BiConsumer<ObjectModel, Point> consumer;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		consumer = mock(BiConsumer.class);
		parser = new VertexComponentParser<>(3, Point::new, consumer);
		model = mock(ObjectModel.class);
	}

	@Test
	void parse() {
		parser.parse(new String[]{"command", "1", "2", "3"}, model);
		verify(consumer).accept(model, new Point(1, 2, 3));
	}

	@Test
	void parseInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"command"}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"command", "2", "3", "4", "5"}, model));
	}
}
