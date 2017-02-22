package org.sarge.jove.model.obj;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;

public class IlluminationParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new GroupParser();
		final int illumination = 42;
		parser.parse(new Scanner(String.valueOf(illumination)), model);
		// TODO
		//verify(model).startNode(name);
	}
}
