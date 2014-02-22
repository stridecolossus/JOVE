package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;

public class TextureCoordObjectLineParserTest {
	private TextureCoordObjectLineParser parser;
	private ObjectModelData data;

	@Before
	public void before() {
		parser = new TextureCoordObjectLineParser();
		data = mock( ObjectModelData.class );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "0.1", "0.2" }, data );
		verify( data ).add( new TextureCoord( 0.1f, 0.2f ) );
	}
}
