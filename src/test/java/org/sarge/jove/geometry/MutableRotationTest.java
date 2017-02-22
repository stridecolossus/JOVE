package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class MutableRotationTest {
	private MutableRotation rot;

	@Before
	public void before() {
		rot = new MutableRotation(Vector.Y_AXIS, 0);
	}

	@Test
	public void constructor() {
		assertEquals(Vector.Y_AXIS, rot.getAxis());
		assertFloatEquals(0, rot.getAngle());
		assertEquals(true, rot.isDirty());
	}

	@Test
	public void setAngle() {
		// Modify and check dirty
		rot.setAngle(MathsUtil.HALF_PI);
		assertEquals(true, rot.isDirty());

		// Query angle and check no longer dirty
		assertFloatEquals(MathsUtil.HALF_PI, rot.getAngle());
		assertEquals(Matrix.rotation(rot), rot.toMatrix());
		assertEquals(false, rot.isDirty());
	}
}
