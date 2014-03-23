package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.MathsUtil;

public class MatrixTest {
	private Matrix matrix;

	@Before
	public void before() {
		matrix = new Matrix( new float[][]{ { 1, 2 }, { 3, 4 } } );
	}

	@Test
	public void identityConstructor() {
		matrix = new Matrix( 4 );
		for( int r = 0; r < 4; ++r ) {
			for( int c = 0; c < 4; ++c ) {
				if( r == c ) {
					assertFloatEquals( 1, matrix.get( r, c ) );
				}
				else {
					assertFloatEquals( 0, matrix.get( r, c ) );
				}
			}
		}
	}

	@Test
	public void getOrder() {
		assertEquals( 2, matrix.getOrder() );
	}

	@Test
	public void get() {
		assertFloatEquals( 1, matrix.get( 0, 0 ) );
		assertFloatEquals( 2, matrix.get( 0, 1 ) );
		assertFloatEquals( 3, matrix.get( 1, 0 ) );
		assertFloatEquals( 4, matrix.get( 1, 1 ) );
	}

	@Test
	public void transpose() {
		final Matrix transpose = new Matrix( new float[][]{ { 1, 3 }, { 2, 4 } } );
		assertEquals( transpose, matrix.transpose() );
	}

	@Test
	public void multiplyScale() {
		final Matrix scaled = new Matrix( new float[][]{ { 3, 6 }, { 9, 12 } } );
		assertEquals( scaled, matrix.multiply( 3 ) );
	}

	@Test
	public void add() {
		final Matrix result = matrix.add( matrix );
		assertEquals( matrix.multiply( 2 ), result );
	}

	@Test
	public void multiply() {
		final Matrix result = matrix.multiply( matrix );
		final Matrix expected = new Matrix( new float[][]{ { 7, 10 }, { 15, 22 } } );
		assertEquals( expected, result );
	}

	@Test
	public void multiplyVector() {
		final float[][] array = new float[ 4 ][ 4 ];
		for( int r = 0; r < 4; ++r ) {
			for( int c = 0; c < 4; ++c ) {
				array[ r ][ c ] = r * 4 + c;
			}
		}
		matrix = new Matrix( array );

		final Vector vec = new Vector( 1, 2, 3 );
		final Vector result = matrix.multiply( vec );
		assertEquals( new Vector( 11, 39, 67 ), result );
	}

	@Test
	public void getSubMatrix() {
		matrix = new Matrix( 4 );
		final Matrix sub = matrix.getSubMatrix( 3 );
		assertNotNull( sub );
		assertEquals( 3, sub.getOrder() );
	}

	@Test
	public void getColumn() {
		// TODO
	}

	@Test
	public void getComponentSize() {
		assertEquals( 2, matrix.getComponentSize() );
	}

	@Test
	public void append() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( 4 );
		matrix.append( buffer );
		buffer.flip();
		assertFloatEquals( 1, buffer.get() );
		assertFloatEquals( 3, buffer.get() );
		assertFloatEquals( 2, buffer.get() );
		assertFloatEquals( 4, buffer.get() );
	}

	@Test
	public void equals() {
		assertEquals( matrix, matrix );
		assertTrue( matrix.equals( matrix ) );
		assertFalse( matrix.equals( false ) );
		assertFalse( matrix.equals( new Matrix( 2 ) ) );
	}

	@Test
	public void translation() {
		final Vector trans = new Vector( 1, 2, 3 );
		final MutableMatrix expected = new MutableMatrix( 4 );
		expected.set( 0, 3, trans.getX() );
		expected.set( 1, 3, trans.getY() );
		expected.set( 2, 3, trans.getZ() );
		assertEquals( expected, Matrix.translation( trans ) );
	}

	@Test
	public void rotation() {
		final Rotation rot = new Rotation( Vector.Y_AXIS, MathsUtil.PI );
		final Quaternion q = new Quaternion( rot );
		final Matrix m = q.toMatrix();
		assertEquals( m, Matrix.rotation( rot ) );
	}

	@Test
	public void scale() {
		final MutableMatrix m = new MutableMatrix( 4 );
		m.set( 0, 0, 4 );
		m.set( 1, 1, 5 );
		m.set( 2, 2, 6 );
		assertEquals( m, Matrix.scale( 4, 5, 6 ) );
	}
}
