package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;

public class GroupParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new GroupParser();
		final String name = "group";
		parser.parse(new Scanner(name), model);
		verify(model).newGroup(name);
	}
}
