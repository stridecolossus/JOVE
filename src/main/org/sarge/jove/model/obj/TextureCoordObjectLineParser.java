package org.sarge.jove.model.obj;

import org.sarge.jove.common.TextureCoord;

/**
 * OBJ parser for texture coordinates.
 * @author Sarge
 */
public class TextureCoordObjectLineParser implements ObjectLineParser {
	private final float[] array = new float[ 2 ];

	@Override
	public void parse( String[] args, ObjectModelData data ) {
		ObjectModelHelper.toArray( args, array );
		final TextureCoord coords = new TextureCoord( array );
		data.add( coords );
	}
}
