package org.sarge.jove.model.obj;

import org.sarge.jove.geometry.Vector;

/**
 * OBJ parser for vertex normals.
 * @author Sarge
 */
public class NormalObjectLineParser implements ObjectLineParser {
	private final float[] array = new float[ 3 ];

	@Override
	public void parse( String[] args, ObjectModelData data ) {
		ObjectModelHelper.toArray( args, array );
		final Vector normal = new Vector( array );
		data.add( normal );
	}
}
