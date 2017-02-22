package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;
import org.sarge.jove.common.TextureCoordinate;

public class TextureCoordinateParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new TextureCoordinateParser();
		parser.parse(new Scanner("0.1 0.2"), model);
		verify(model).add(new TextureCoordinate(0.1f, 0.2f));
	}
}
