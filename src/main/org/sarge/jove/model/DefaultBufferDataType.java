package org.sarge.jove.model;

import org.sarge.jove.common.Appendable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * General VBO component types.
 */
public enum DefaultBufferDataType implements BufferDataType {
	VERTICES( Point.SIZE ),
	NORMALS( Vector.SIZE ),
	COLOURS( Colour.SIZE );

	private final int size;

	private DefaultBufferDataType( int size ) {
		this.size = size;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public Appendable getData( Vertex v ) {
		switch( this ) {
		case VERTICES:	return v.getPosition();
		case NORMALS:	return v.getNormal();
		case COLOURS:	return v.getColour();
		}

		throw new UnsupportedOperationException( "Invalid buffer data-type: " + this );
	}
}
