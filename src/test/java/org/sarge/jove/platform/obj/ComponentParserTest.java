package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class ComponentParserTest {
	private Parser parser;
	private ObjectModel model;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		parser = new ComponentParser<>(3, Point::new, ObjectModel::vertices);
		model = mock(ObjectModel.class);
		when(model.vertices()).thenReturn(mock(List.class));
	}

	@Test
	void constructor() {
		assertNotNull(parser);
	}

	@Test
	void array() {
		parser.parse(new String[]{"1", "2", "3"}, model);
		verify(model.vertices()).add(new Point(1, 2, 3));
	}

	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"1", "2", "3", "4"}, model));
	}
}
