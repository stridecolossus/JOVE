package org.sarge.jove.geometry;

import java.nio.FloatBuffer;

import org.sarge.jove.util.MathsUtil;

/**
 * 2D floating-point matrix.
 * <p>
 * These matrices are:
 * <ul>
 * <li>square - i.e. same number of rows and columns</li>
 * <li>row major - i.e. elements are accessed by row <b>then</b> by column</li> (however @see {@link #append(FloatBuffer)})
 * <li>immutable - all mutators create a <b>new</b> instance</li>
 * </ul>
 * @author Sarge
 */
public class Matrix implements Transform {
	/**
	 * 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = new Matrix( 4 );

	/**
	 * Creates a translation matrix using the given vector.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation( Tuple vec ) {
		final MutableMatrix trans = new MutableMatrix( 4 );
		trans.setColumn( 3, vec );
		return trans;
	}

	/**
	 * Creates a rotation matrix about the given axis.
	 * @param rot Axis-angle rotation
	 * @return Rotation matrix
	 */
	public static Matrix rotation( Rotation rot ) {
		// Calc angles
		final float angle = -rot.getAngle();
		final float sin = MathsUtil.sin( angle );
		final float cos = MathsUtil.cos( angle );

		// Init matrix
		final MutableMatrix m = new MutableMatrix( 4 );

		// Rotate by axis
		if( rot.getAxis() == Vector.X_AXIS ) {
			m.set( 1, 1, cos );
			m.set( 1, 2, sin );
			m.set( 2, 1, -sin );
			m.set( 2, 2, cos );
		}
		else
		if( rot.getAxis() == Vector.Y_AXIS ) {
			m.set( 0, 0, cos );
			m.set( 0, 2, -sin );
			m.set( 2, 0, sin );
			m.set( 2, 2, cos );
		}
		else
		if( rot.getAxis() == Vector.Z_AXIS ) {
			m.set( 0, 0, cos );
			m.set( 0, 1, -sin );
			m.set( 1, 0, sin );
			m.set( 1, 1, cos );
		}
		else {
			throw new UnsupportedOperationException( "Arbitrary rotation axis not implemented" );
		}

		return m;
	}

	/**
	 * Creates a scaling matrix.
	 * @param x X scale
	 * @param y Y scale
	 * @param z Z scale
	 * @return Scaling matrix
	 */
	public static Matrix scale( float x, float y, float z ) {
		final MutableMatrix scale = new MutableMatrix( 4 );
		scale.set( 0, 0, x );
		scale.set( 1, 1, y );
		scale.set( 2, 2, z );
		return scale;
	}

	/**
	 * Creates a matrix that is scaled by the given value in all three directions.
	 * @param scale Scalar
	 * @return Scaling matrix
	 */
	public static Matrix scale( float scale ) {
		return scale( scale, scale, scale );
	}

	protected final float[][] matrix;

	private final float[] tupleArray = new float[ 4 ];
	private final float[] homogenousArray = new float[ 4 ];

	/**
	 * Constructor.
	 * @param matrix 2D matrix data
	 */
	public Matrix( float[][] matrix ) {
		if( matrix.length != matrix[ 0 ].length ) throw new IllegalArgumentException( "Matrix is not square" );
		this.matrix = matrix.clone();
	}

	/**
	 * Constructor for an identity matrix.
	 * @param order Order
	 */
	public Matrix( int order ) {
		matrix = new float[ order ][ order ];

		for( int n = 0; n < order; ++n ) {
			matrix[ n ][ n ] = 1;
		}
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int getOrder() {
		return matrix.length;
	}

	/**
	 * Retrieves the matrix value at the given row and column.
	 * @param r Row
	 * @param c Column
	 * @return Matrix value
	 */
	public float get( int r, int c ) {
		return matrix[ r ][ c ];
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final Matrix m = new Matrix( matrix.length );

		for( int r = 0; r < matrix.length; ++r ) {
			for( int c = 0; c < matrix.length; ++c ) {
				m.matrix[ r ][ c ] = this.matrix[ c ][ r ];
			}
		}

		return m;
	}

	/**
	 * Multiplies this matrix by the given scaling value.
	 * @param scale Scaling value
	 * @return Scaled matrix
	 */
	public Matrix multiply( float scale ) {
		final Matrix m = new Matrix( matrix.length );

		for( int r = 0; r < matrix.length; ++r ) {
			for( int c = 0; c < matrix.length; ++c ) {
				m.matrix[ r ][ c ] = this.matrix[ r ][ c ] * scale;
			}
		}

		return m;
	}

	/**
	 * Adds a matrix to this matrix.
	 * @param m Matrix to add
	 * @return Summed matrix
	 * @throws IllegalArgumentException if the matrices are not of the same order
	 */
	public Matrix add( Matrix m ) {
		if( m.getOrder() != this.getOrder() ) throw new IllegalArgumentException( "Matrices must have same order" );

		final Matrix result = new Matrix( matrix.length );

		for( int r = 0; r < matrix.length; ++r ) {
			for( int c = 0; c < matrix.length; ++c ) {
				result.matrix[ r ][ c ] = this.matrix[ r ][ c ] + m.matrix[ r ][ c ];
			}
		}

		return result;
	}

	/**
	 * Multiplies this matrix with another matrix.
	 * @param m Matrix
	 * @return Multiplied matrix
	 * @throws IllegalArgumentException if the matrices are not of the same order
	 */
	public Matrix multiply( Matrix m ) {
		if( m.getOrder() != this.getOrder() ) throw new IllegalArgumentException( "Matrices must have same order" );

		final Matrix result = new Matrix( matrix.length );

		float total;
		for( int r = 0; r < matrix.length; ++r ) {
			for( int c = 0; c < matrix.length; ++c ) {
				total = 0;
				for( int n = 0; n < matrix.length; ++n ) {
					total += this.matrix[ r ][ n ] * m.matrix[ n ][ c ];
				}
				result.matrix[ r ][ c ] = total;
			}
		}

		return result;
	}

	/**
	 * Multiplies the given vector by this matrix.
	 * @param v Vector to multiply
	 * @return Multiplied vector
	 * @throws IllegalArgumentException if this matrix is too small
	 */
	public Vector multiply( Vector v ) {
		multiplyTuple( v );
		return new Vector( homogenousArray );
	}

	/**
	 * Multiplies the given point by this matrix.
	 * @param pos Point to multiply
	 * @return Multiplied point
	 * @throws IllegalArgumentException if this matrix is too small
	 */
	public Point multiply( Point pos ) {
		multiplyTuple( pos );
		return new Point( homogenousArray );
	}

	/**
	 * Helper.
	 */
	private void multiplyTuple( Tuple t ) {
		if( getOrder() != 4 ) throw new IllegalArgumentException( "Matrix must have an order of 4 to multiply a vector or point" );

		// Convert to homogeneous vector
		t.toArray( tupleArray );
		tupleArray[ 3 ] = 1;

		// Multiple by matrix
		float total;
		for( int r = 0; r < matrix.length; ++r ) {
			total = 0;
			for( int c = 0; c < matrix.length; ++c ) {
				total += matrix[ r ][ c ] * tupleArray[ c ];
			}
			homogenousArray[ r ] = total;
		}
	}

	/**
	 * Extracts a portion of this matrix (top-left).
	 * @param size Sub-matrix size
	 * @return Sub-matrix
	 */
	public Matrix getSubMatrix( int size ) {
		if( size >= getOrder() ) throw new IllegalArgumentException( "Sub-matrix too large" );

		final Matrix m = new Matrix( size );
		for( int r = 0; r < size; ++r ) {
			for( int c = 0; c < size; ++c ) {
				m.matrix[ r ][ c ] = this.matrix[ r ][ c ];
			}
		}
		return m;
	}

	/**
	 * Adds this matrix to the given NIO buffer in OpenGL <b>column-major</b> order.
	 * @param buffer
	 */
	public void append( FloatBuffer buffer ) {
		for( int c = 0; c < matrix.length; ++c ) {
			for( int r = 0; r < matrix.length; ++r ) {
				buffer.put( matrix[ r ][ c ] );
			}
		}
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof Matrix ) {
			final Matrix m = (Matrix) obj;
			if( m.getOrder() != this.getOrder() ) return false;
			for( int r = 0; r < matrix.length; ++r ) {
				for( int c = 0; c < matrix.length; ++c ) {
					if( !MathsUtil.isEqual( this.matrix[ r ][ c ], m.matrix[ r ][ c ] ) ) return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Matrix toMatrix() {
		return this;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for( int r = 0; r < matrix.length; ++r ) {
			if( r > 0 ) sb.append( '\n' );
			for( int c = 0; c < matrix.length; ++c ) {
				if( c > 0 ) sb.append( ',' );
				sb.append( String.format( "%10.5f", matrix[ r ][ c ] ) );
			}
		}
		return sb.toString();
	}
}
