package org.sarge.jove.model.obj;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;

public class TextureParserTest extends AbstractParserTest {
	@Test
	public void parse() throws IOException {
		final String id = "id";
		final Parser parser = new TextureParser(id);
		final String path = "path";
		parser.parse(new Scanner(path), model);
		verify(mat).addTexture(id, path);
	}
}
