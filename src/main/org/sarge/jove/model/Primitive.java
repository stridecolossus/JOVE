package org.sarge.jove.model;

/**
 * Drawing primitives.
 * @author Sarge
 */
public enum Primitive {
	POINTS( 1, 1 ),
	LINES( 2, 2 ),
	LINE_STRIP( 2, 1 ),
	TRIANGLES( 3, 3 ),
	TRIANGLE_STRIP( 3, 1 ),
	TRIANGLE_FAN( 1, 1 );		// TODO - uses first 3 for initial triangle then 1 vertex for subsequent ones (using vertex 0,1)

	private final int size;
	private final int stride;

	private Primitive( int size, int stride ) {
		this.size = size;
		this.stride = stride;
	}

	/**
	 * @return Size of this primitive (number of vertices per face)
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return Vertex buffer stride for this primitive
	 */
	public int getStride() {
		return stride;
	}

	/**
	 * @return Whether this primitive supports normals
	 */
	public boolean hasNormals() {
		switch( this ) {
		case TRIANGLES:
		case TRIANGLE_STRIP:
		case TRIANGLE_FAN:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return Whether this primitive is a strip
	 */
	public boolean isStrip() {
		switch( this ) {
		case TRIANGLE_STRIP:
		case LINE_STRIP:
		case TRIANGLE_FAN:
			return true;

		default:
			return false;
		}
	}

	/**
	 * Calculates the number of faces using this primitive for the given number of vertices.
	 * @param num Number of vertices
	 * @return Number of faces
	 */
	public int getFaceCount( int num ) {
		switch( this ) {
		case POINTS:
			return num;

		case LINES:
			return num / 2;

		case LINE_STRIP:
			return num - 1;

		case TRIANGLES:
			return num / 3;

		case TRIANGLE_STRIP:
		case TRIANGLE_FAN:
		default:
			return num - 2;
		}
	}

	/**
	 * Tests whether the given number of vertices is valid for this primitive.
	 * @param num Vertex count
	 * @return <tt>true</tt> if the given number of vertices is correct for this primitive
	 */
	public boolean isValidVertexCount( int num ) {
		switch( this ) {
		case LINES:
			return ( num % 2 ) == 0;

		case TRIANGLES:
			return ( num % 3 ) == 0;

		default:
			if( ( num > 0 ) && ( getFaceCount( num ) < 1 ) ) return false;
			return true;
		}
	}
}
