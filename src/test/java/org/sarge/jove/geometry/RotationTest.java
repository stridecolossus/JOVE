package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class RotationTest {
	private Rotation rot;

	@Before
	public void before() {
		rot = new Rotation( Vector.Y_AXIS, 0 );
	}

	@Test
	public void constructor() {
		assertEquals( Vector.Y_AXIS, rot.getAxis() );
		assertFloatEquals( 0, rot.getAngle() );
		assertEquals( true, rot.isDirty() );
	}

	@Test
	public void setAngle() {
		// Modify and check dirty
		rot.setAngle( 1f );
		assertTrue( rot.isDirty() );

		// Query angle and check no longer dirty
		assertFloatEquals( 1f, rot.getAngle() );
		assertEquals( Matrix.rotation( new Rotation( Vector.Y_AXIS, 1f ) ), rot.toMatrix() );
		assertEquals( false, rot.isDirty() );
	}
}
