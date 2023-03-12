package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

public class FaceParserTest {
	private FaceParser parser;
	private ObjectModel model;

	@BeforeEach
	void before() {
		parser = new FaceParser();
		model = mock(ObjectModel.class);
		when(model.components()).thenReturn(3);
	}

	@Test
	void position() {
		when(model.components()).thenReturn(1);
		parser.parse("1 1 1", model);
		verify(model, times(3)).vertex(new int[]{1, 0, 0});
	}

	@Test
	void coordinate() {
		when(model.components()).thenReturn(2);
		parser.parse("1/2 1/2 1/2", model);
		verify(model, times(3)).vertex(new int[]{1, 2, 0});
	}

	@Test
	void normal() {
		when(model.components()).thenReturn(2);
		parser.parse("1//3 1//3 1//3", model);
		verify(model, times(3)).vertex(new int[]{1, 0, 3});
	}

	@Test
	void all() {
		when(model.components()).thenReturn(3);
		parser.parse("1/2/3 1/2/3 1/2/3", model);
		verify(model, times(3)).vertex(new int[]{1, 2, 3});
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(StringUtils.EMPTY, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2", model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4", model));
	}

	@Test
	void length() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1/2/3/4 1 2", model));
	}

	@Test
	void components() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1/2/3/4 1 2", model));
	}
}
