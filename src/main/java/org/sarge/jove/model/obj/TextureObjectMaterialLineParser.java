package org.sarge.jove.model.obj;

import java.io.IOException;

import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.lib.util.Check;

/**
 * Parser for material texture maps.
 * @author Sarge
 */
public class TextureObjectMaterialLineParser implements ObjectMaterialLineParser {
	private final String name;
	private final TextureLoader loader;

	/**
	 * Constructor.
	 * @param name		Texture parameter name
	 * @param loader	Texture loader
	 */
	public TextureObjectMaterialLineParser( String name, TextureLoader loader ) {
		Check.notEmpty( name );
		Check.notNull( loader );

		this.name = name;
		this.loader = loader;
	}

	@Override
	public void parse( String[] args, MutableMaterial mat ) {
		// Extract image file-path
		final String path = ObjectModelHelper.toString( args, "Expected texture file-path" );

		// Load texture image
		final Texture tex;
		try {
			tex = loader.load( path );
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}

		// Add to material
		mat.set( name, tex );
	}
}
