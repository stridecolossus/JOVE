package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParserTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = mock(ObjectModel.class);
	}

	@Test
	void ignore() {
		Parser.IGNORE.parse(null, model);
		verifyNoMoreInteractions(model);
	}

	@Test
	void group() {
		final String name = "name";
		Parser.GROUP.parse(new String[]{name}, model);
		verify(model).start();
//		verify(model).name(name);
	}

	@Test
	void groupAnonymous() {
		Parser.GROUP.parse(new String[]{}, model);
		verify(model).start();
		verifyNoMoreInteractions(model);
	}

	@Test
	void trim() {
		final String[] array = new String[]{" text "};
		Parser.trim(array);
		assertArrayEquals(new String[]{"text"}, array);
	}
}
