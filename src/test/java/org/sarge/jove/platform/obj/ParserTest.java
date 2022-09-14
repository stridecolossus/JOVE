package org.sarge.jove.platform.obj;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

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
		Parser.GROUP.parse(name, model);
		verify(model).start();
//		verify(model).name(name);
	}

	@Test
	void groupAnonymous() {
		Parser.GROUP.parse(null, model);
		verify(model).start();
		verifyNoMoreInteractions(model);
	}
}
