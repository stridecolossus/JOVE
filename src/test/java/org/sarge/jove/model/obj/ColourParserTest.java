package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;

public class ColourParserTest extends AbstractParserTest {
	private static final Colour col = new Colour(0.1f, 0.2f, 0.3f, 1);

	private ColourParser parser;

	@Before
	public void before() {
		parser = new ColourParser("col");
	}

	@Test
	public void parse() {
		parser.parse(new Scanner("0.1 0.2 0.3"), model);
		verify(mat).addColour("col", col);
	}

	@Test
	public void parseAlpha() {
		parser.parse(new Scanner("0.1 0.2 0.3 1"), model);
		verify(mat).addColour("col", col);
	}
}
