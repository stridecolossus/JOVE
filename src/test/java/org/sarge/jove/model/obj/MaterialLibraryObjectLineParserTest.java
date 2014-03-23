package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.material.Material;

public class MaterialLibraryObjectLineParserTest {
	private MaterialLibraryObjectLineParser parser;
	private ObjectMaterialLoader loader;

	@Before
	public void before() {
		loader = mock( ObjectMaterialLoader.class );
		parser = new MaterialLibraryObjectLineParser( loader );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "material" }, mock( ObjectModelData.class ) );
		verify( loader ).load( "material", new HashMap<String, Material>() );
	}
}
