package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class VectorTest {
	private Vector vec;

	@Before
	public void before() {
		vec = new Vector( 1, 2, 3 );
	}

	@Test
	public void getMagnitudeSquared() {
		assertFloatEquals( 1 * 1 + 2 * 2 + 3 * 3, vec.getMagnitudeSquared() );
	}

	@Test
	public void add() {
		final Vector sum = vec.add( new Vector( 4, 5, 6 ) );
		assertEquals( new Vector( 5, 7, 9 ), sum );
	}

	@Test
	public void multiply() {
		final Vector result = vec.multiply( 3 );
		assertEquals( new Vector( 3, 6, 9 ), result );
	}

	@Test
	public void normalize() {
		assertFloatEquals( 1, vec.normalize().getMagnitudeSquared() );
	}

	@Test
	public void crossProduct() {
		final Vector cross = Vector.X_AXIS.cross( Vector.Y_AXIS );
		assertEquals( Vector.Z_AXIS, cross );
	}

	@Test
	public void angle() {
		final float angle = Vector.X_AXIS.angle( Vector.Y_AXIS );
		assertFloatEquals( MathsUtil.HALF_PI, angle );
	}

	@Test
	public void project() {
		assertEquals( new Vector( 1, 0, 0 ), Vector.X_AXIS.project( vec ) );
		assertEquals( new Vector( 0, 2, 0 ), Vector.Y_AXIS.project( vec ) );
		assertEquals( new Vector( 0, 0, 3 ), Vector.Z_AXIS.project( vec ) );
	}

	@Test
	public void reflect() {
		final Vector normal = new Vector( 1, 1, 1 ).normalize();
		assertEquals( new Vector( -3, -2, -1 ), vec.reflect( normal ) );
	}
}
