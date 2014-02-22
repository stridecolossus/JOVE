package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.material.MutableMaterial;

public class ColourObjectMaterialLineParserTest {
	private static final Colour col = new Colour( 0.1f, 0.2f, 0.3f, 1 );

	private ColourObjectMaterialLineParser parser;
	private MutableMaterial mat;

	@Before
	public void before() {
		parser = new ColourObjectMaterialLineParser( "col" );
		mat = mock( MutableMaterial.class );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "0.1", "0.2", "0.3" }, mat );
		verify( mat ).set( "col", col );
	}

	@Test
	public void parseAlpha() {
		parser.parse( new String[]{ "0.1", "0.2", "0.3", "1" }, mat );
		verify( mat ).set( "col", col );
	}
}
