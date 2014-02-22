package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class GroupObjectLineParserTest {
	private GroupObjectLineParser parser;
	private ObjectModelData data;

	@Before
	public void before() {
		parser = new GroupObjectLineParser();
		data = mock( ObjectModelData.class );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "name" }, data );
		verify( data ).startNode( "name" );
	}
}
