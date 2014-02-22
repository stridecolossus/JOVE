package org.sarge.jove.model.obj;

import org.sarge.jove.material.Material;

/**
 * Use material line parser.
 * @author Sarge
 */
public class UseMaterialObjectLineParser implements ObjectLineParser {
	@Override
	public void parse( String[] args, ObjectModelData data ) {
		// Lookup material
		final String name = ObjectModelHelper.toString( args, "Expected material name" );
		final Material mat = data.getMaterialLibrary().get( name );
		if( mat == null ) throw new IllegalArgumentException( "Unknown material: " + name );

		// Add to node
		data.getNode().setMaterial( mat );
	}
}
