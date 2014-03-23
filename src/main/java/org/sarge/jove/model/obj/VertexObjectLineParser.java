package org.sarge.jove.model.obj;

import org.sarge.jove.geometry.Point;

/**
 * OBJ parser for vertex data.
 * @author Sarge
 */
public class VertexObjectLineParser implements ObjectLineParser {
	private final float[] array = new float[ 3 ];

	@Override
	public void parse( String[] args, ObjectModelData data ) {
		ObjectModelHelper.toArray( args, array );
		final Point pos = new Point( array );
		data.add( pos );
	}
}
