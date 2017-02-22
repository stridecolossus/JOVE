package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class QuaternionTest {
	private Rotation rot;
	private Quaternion q;

	@Before
	public void before() {
		rot = new Rotation(Vector.Y_AXIS, MathsUtil.HALF_PI);
		q = new Quaternion(rot);
	}

	@Test
	public void getMatrix() {
		assertEquals(rot.toMatrix(), q.toMatrix());
	}

	@Test
	public void normalise() {
		q.normalize();
	}

	@Test
	public void toRotation() {
		assertEquals(rot, q.toRotation());
	}

	@Test
	public void multiply() {
		final Matrix y = Matrix.rotation(new Rotation(Vector.Y_AXIS, MathsUtil.HALF_PI));
		final Matrix x = Matrix.rotation(new Rotation(Vector.X_AXIS, MathsUtil.PI));
		final Matrix expected = y.multiply(x);
		final Quaternion result = q.multiply(new Quaternion(new Rotation(Vector.X_AXIS, MathsUtil.PI)));
		assertEquals(expected, result.toMatrix());
	}

	@Test
	public void rotate() {
		final Vector vec = q.rotate(Vector.X_AXIS);
		assertEquals(new Vector(0, 0, -1), vec);
	}

	@Test
	public void conjugate() {
		q = new Quaternion(1, 2, 3, 4);
		assertEquals(new Quaternion(1, -2, -3, -4), q.conjugate());
	}
}
