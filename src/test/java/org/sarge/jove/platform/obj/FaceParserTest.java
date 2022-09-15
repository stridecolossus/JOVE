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
	}

	@Test
	void parsePosition() {
		parser.parse("1 1 1", model);
		verify(model, times(3)).vertex(1, null, null);
	}

	@Test
	void parsePositionTexture() {
		parser.parse("1/1 1/1 1/1", model);
		verify(model, times(3)).vertex(1, null, 1);
	}

	@Test
	void parsePositionTextureNormal() {
		parser.parse("1/1/1 1/1/1 1/1/1", model);
		verify(model, times(3)).vertex(1, 1, 1);
	}

	@Test
	void parsePositionNormal() {
		parser.parse("1//1 1//1 1//1", model);
		verify(model, times(3)).vertex(1, 1, null);
	}

	@Test
	void parseInvalidFaceLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(StringUtils.EMPTY, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2", model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4", model));
	}

	@Test
	void parseInvalidFaceComponents() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse("1/2/3/4 1 2", model));
	}
}
