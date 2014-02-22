package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.lib.io.DataSource;

public class UseMaterialObjectLineParserTest {
	private UseMaterialObjectLineParser parser;
	private ObjectModelData model;

	@Before
	public void before() {
		parser = new UseMaterialObjectLineParser();
		model = new ObjectModelData( mock( DataSource.class ), mock( RenderingSystem.class ) );
	}

	@Test
	public void parse() {
		// Add a material
		final Material mat = new MutableMaterial( "mat" );
		model.add( mat );

		// Check material added to node
		parser.parse( new String[]{ "mat" }, model );
		// TODO - check material added to node
	}

	@Test( expected = IllegalArgumentException.class )
	public void parseUnknownMaterial() {
		parser.parse( new String[]{ "cobblers" }, model );
	}
}
