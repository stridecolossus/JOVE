package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

public class UseMaterialObjectLineParserTest extends AbstractParserTest {
	private Parser parser;
	
	@Before
	public void before() {
		parser = new UseMaterialParser();
	}
	
	@Test
	public void parse() throws IOException {
		// Add a material
		final String name = "mat";
		final ObjectMaterial mat = mock(ObjectMaterial.class);
		when(model.getMaterial(name)).thenReturn(mat);
		
		// Use material and check added to current group
		parser.parse(new Scanner(name), model);
		verify(group).setMaterial(mat);
	}

	@Test( expected = IllegalArgumentException.class )
	public void parseUnknownMaterial() throws IOException {
		parser.parse(new Scanner("cobblers"), model);
	}
}
