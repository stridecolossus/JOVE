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
		super( m );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MutableMatrix getResultMatrix() {
		dirty = true;
		return this;
	}

	@Override
	public MutableMatrix transpose() {
		super.transpose( matrix.clone(), this );
		dirty = true;
		return this;
	}

	@Override
	public MutableMatrix multiply( Matrix m ) {
		super.multiply( matrix.clone(), m, this );
		dirty = true;
		return this;
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
