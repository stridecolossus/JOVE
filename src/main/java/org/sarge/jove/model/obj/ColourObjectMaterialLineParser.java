package org.sarge.jove.model.obj;

import org.sarge.jove.common.Colour;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.lib.util.Check;

/**
 * Parser for material colours.
 * @author Sarge
 */
public class ColourObjectMaterialLineParser implements ObjectMaterialLineParser {
	private final String name;
	private final float[] three = new float[ 3 ];
	private final float[] four = new float[ 4 ];

	/**
	 * Constructor.
	 * @param name Colour parameter name
	 */
	public ColourObjectMaterialLineParser( String name ) {
		Check.notEmpty( name );
		this.name = name;
	}

	@Override
	public void parse( String[] args, MutableMaterial mat ) {
		final Colour col;
		if( args.length == 3 ) {
			ObjectModelHelper.toArray( args, three );
			col = new Colour( three );
		}
		else {
			ObjectModelHelper.toArray( args, four );
			col = new Colour( four );
		}
		mat.set( name, col );
	}
}
