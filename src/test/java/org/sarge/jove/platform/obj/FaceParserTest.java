package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		parser.parse(new String[]{"1", "1", "1"}, model);
		verify(model, times(3)).vertex(new Integer[]{1, null, null});
	}

	@Test
	void parsePositionTexture() {
		parser.parse(new String[]{"1/1", "1/1", "1/1"}, model);
		verify(model, times(3)).vertex(new Integer[]{1, 1, null});
	}

	@Test
	void parsePositionTextureNormal() {
		parser.parse(new String[]{"1/1/1", "1/1/1", "1/1/1"}, model);
		verify(model, times(3)).vertex(new Integer[]{1, 1, 1});
	}

	@Test
	void parsePositionNormal() {
		parser.parse(new String[]{"1//1", "1//1", "1//1"}, model);
		verify(model, times(3)).vertex(new Integer[]{1, null, 1});
	}

	@Test
	void parseInvalidFaceLength() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"1"}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"1", "2", "3", "4"}, model));
	}

	@Test
	void parseInvalidFaceComponents() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"", "1", "1"}, model));
		assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"1/2/3/4", "1", "1"}, model));
	}
}
