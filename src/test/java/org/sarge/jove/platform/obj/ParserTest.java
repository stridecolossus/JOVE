package org.sarge.jove.platform.obj;

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
		Parser.GROUP.parse(null, model);
		verify(model).start();
	}
}