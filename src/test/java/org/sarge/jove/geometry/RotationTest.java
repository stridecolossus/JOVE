package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class RotationTest {
	private Rotation rot;

	@Before
	public void before() {
		rot = new Rotation(Vector.Y_AXIS, MathsUtil.HALF_PI);
	}

	@Test
	public void constructor() {
		assertEquals(Vector.Y_AXIS, rot.getAxis());
		assertFloatEquals(MathsUtil.HALF_PI, rot.getAngle());
		assertEquals(Matrix.rotation(rot), rot.toMatrix());
		assertEquals(false, rot.isDirty());
	}
}
