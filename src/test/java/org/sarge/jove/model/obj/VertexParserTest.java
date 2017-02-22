package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class VertexParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new VertexParser();
		parser.parse(new Scanner("0.1 0.2 0.3"), model);
		verify(model).add(new Point(0.1f, 0.2f, 0.3f));
	}
}
