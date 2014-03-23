package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Test;

public class MathsUtilTest {
	@Test
	public void sqrt() {
		assertFloatEquals( (float) Math.sqrt( 42 ), MathsUtil.sqrt( 42 ) );
	}

	@Test
	public void isPowerOfTwo() {
		assertTrue( MathsUtil.isPowerOfTwo( 1 ) );
		assertTrue( MathsUtil.isPowerOfTwo( 2 ) );
		assertTrue( MathsUtil.isPowerOfTwo( 4 ) );
		assertTrue( MathsUtil.isPowerOfTwo( 8 ) );
		assertTrue( MathsUtil.isPowerOfTwo( 16 ) );
		assertFalse( MathsUtil.isPowerOfTwo( 0 ) );
		assertFalse( MathsUtil.isPowerOfTwo( 3 ) );
	}

	@Test
	public void isZero() {
		assertTrue( MathsUtil.isZero( 0 ) );
		assertTrue( MathsUtil.isZero( 0.000001f ) );
		assertFalse( MathsUtil.isZero( 0.001f ) );
	}

	@Test
	public void isFloatEqual() {
		assertTrue( MathsUtil.isEqual( 0, 0 ) );
		assertTrue( MathsUtil.isEqual( 0.000001f, 0.000001f ) );
		assertFalse( MathsUtil.isEqual( 0.0001f, 0.000001f ) );
	}

	@Test
	public void clamp() {
		assertFloatEquals( 0, MathsUtil.clamp( 0 ) );
		assertFloatEquals( 0.5f, MathsUtil.clamp( 0.5f ) );
		assertFloatEquals( 1, MathsUtil.clamp( 1 ) );
		assertFloatEquals( 0, MathsUtil.clamp( -1 ) );
		assertFloatEquals( 1, MathsUtil.clamp( 2 ) );
	}

	@Test
	public void isEven() {
		assertTrue( MathsUtil.isEven( 2 ) );
		assertFalse( MathsUtil.isEven( 3 ) );
	}

	@Test
	public void convertFloatArray() {
		final float[] array = MathsUtil.convert( "1, 2, 3", 3 );
		assertNotNull( array );
		assertEquals( 3, array.length );
		for( int n = 0; n < 3; ++n ) {
			assertFloatEquals( n + 1, array[ n ] );
		}
	}

	@Test
	public void toDegrees() {
		assertFloatEquals( 90, MathsUtil.toDegrees( MathsUtil.HALF_PI ) );
	}

	@Test
	public void toRadians() {
		assertFloatEquals( MathsUtil.HALF_PI, MathsUtil.toRadians( 90 ) );
	}

	@Test
	public void trigFunctions() {
		assertFloatEquals( (float) Math.sin( 0.5f ), MathsUtil.sin( 0.5f ) );
		assertFloatEquals( (float) Math.cos( 0.5f ), MathsUtil.cos( 0.5f ) );
		assertFloatEquals( (float) Math.tan( 0.5f ), MathsUtil.tan( 0.5f ) );
		assertFloatEquals( (float) Math.asin( 0.5f ), MathsUtil.asin( 0.5f ) );
		assertFloatEquals( (float) Math.acos( 0.5f ), MathsUtil.acos( 0.5f ) );
		assertFloatEquals( (float) Math.atan( 0.5f ), MathsUtil.atan( 0.5f ) );
	}
}
