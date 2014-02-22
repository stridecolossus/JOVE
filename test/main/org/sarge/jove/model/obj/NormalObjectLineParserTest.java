package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class NormalObjectLineParserTest {
	private NormalObjectLineParser parser;
	private ObjectModelData data;

	@Before
	public void before() {
		parser = new NormalObjectLineParser();
		data = mock( ObjectModelData.class );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "0.1", "0.2", "0.3" }, data );
		verify( data ).add( new Vector( 0.1f, 0.2f, 0.3f ) );
	}
}
