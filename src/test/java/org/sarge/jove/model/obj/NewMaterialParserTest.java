package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;

public class NewMaterialParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final Parser parser = new NewMaterialParser();
		final String name = "name";
		parser.parse(new Scanner(name), model);
		verify(model).newMaterial(name);
	}
}
