package org.sarge.jove.model.obj;

import org.sarge.jove.material.MutableMaterial;

/**
 * Parser for material illumination model.
 * @author Sarge
 */
public class IlluminationModelObjectMaterialLineParser implements ObjectMaterialLineParser {
	@Override
	public void parse( String[] args, MutableMaterial mat ) {
		if( args.length != 1 ) throw new IllegalArgumentException( "Expected illumination model index" );
//		final int model = Integer.parseInt( args[ 0 ] );
		// TODO
	}
}
