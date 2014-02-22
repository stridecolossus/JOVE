package org.sarge.jove.geometry;

/**
 * Mutable implementation with helper methods.
 * @author Sarge
 */
public class MutableMatrix extends Matrix {
	private boolean dirty = true;

	/**
	 * Identity constructor.
	 * @param order Matrix order
	 */
	public MutableMatrix( int order ) {
		super( order );
	}

	/**
	 * Copy constructor.
	 * @param m Matrix
	 */
	public MutableMatrix( Matrix m ) {
		super( m.matrix );
	}

	/**
	 * Sets the specified matrix element.
	 * @param r Row
	 * @param c Column
	 * @param f Value to set
	 */
	public void set( int r, int c, float f ) {
		matrix[ r ][ c ] = f;
		dirty = true;
	}

	/**
	 * Gets the left-most 3 elements of the given row as a vector.
	 * @param row Row index
	 * @return Vector
	 */
	public Vector getRow( int row ) {
		return new Vector( matrix[ row ][ 0 ], matrix[ row ][ 1 ], matrix[ row ][ 2 ] );
	}

	/**
	 * Sets the first 3 values of a row to the given vector.
	 * @param row	Row to set
	 * @param vec	Vector
	 */
	public void setRow( int row, Tuple vec ) {
		matrix[ row ][ 0 ] = vec.x;
		matrix[ row ][ 1 ] = vec.y;
		matrix[ row ][ 2 ] = vec.z;
		dirty = true;
	}

	/**
	 * Gets the top-most 3 values of the given column as a vector.
	 * @param col Column index
	 * @return Vector
	 */
	public Vector getColumn( int col ) {
		return new Vector( matrix[ 0 ][ col ], matrix[ 1 ][ col ], matrix[ 2 ][ col ] );
	}

	/**
	 * Sets the top 3 values of a column to the given vector.
	 * @param col	Column to set
	 * @param vec	Vector
	 */
	public void setColumn( int col, Tuple vec ) {
		matrix[ 0 ][ col ] = vec.x;
		matrix[ 1 ][ col ] = vec.y;
		matrix[ 2 ][ col ] = vec.z;
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Matrix toMatrix() {
		dirty = false;
		return this;
	}
}
