package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class NormalParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new NormalParser();
		parser.parse(new Scanner("1 2 3"), model);
		verify(model).add(new Vector(1, 2, 3));
	}
}
