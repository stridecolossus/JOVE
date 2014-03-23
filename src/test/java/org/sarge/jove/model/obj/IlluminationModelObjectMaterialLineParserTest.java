package org.sarge.jove.model.obj;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.material.MutableMaterial;

public class IlluminationModelObjectMaterialLineParserTest {
	private IlluminationModelObjectMaterialLineParser parser;
	private MutableMaterial mat;

	@Before
	public void before() {
		parser = new IlluminationModelObjectMaterialLineParser();
		mat = new MutableMaterial( "mat" );
	}

	@Test
	public void parse() {
		parser.parse( new String[]{ "42" }, mat );
		// TODO
	}
}
