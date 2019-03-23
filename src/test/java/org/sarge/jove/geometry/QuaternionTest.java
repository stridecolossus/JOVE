package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.MathsUtil;

public class QuaternionTest {
	private Quaternion quaternion;

	@BeforeEach
	public void before() {
		quaternion = new Quaternion(0, 0, 1, 0);
	}

	@Test
	public void constructor() {
		assertEquals(0, quaternion.w);
		assertEquals(0, quaternion.x);
		assertEquals(1, quaternion.y);
		assertEquals(0, quaternion.z);
	}

	@Test
	public void rotation() {
		final Rotation rot = Rotation.of(Vector.Y_AXIS, MathsUtil.PI);
		assertEquals(quaternion, Quaternion.of(rot));
	}

	@Test
	public void magnitude() {
		assertEquals(1f, quaternion.magnitude(), 0.0001f);
	}

	@Test
	public void matrix() {
		final Matrix expected = Matrix.rotation(Vector.Y_AXIS, MathsUtil.PI);
		assertEquals(expected, quaternion.matrix());
	}

	@Test
	public void normalize() {
		assertEquals(quaternion, quaternion.normalize());
		assertEquals(quaternion, new Quaternion(0, 0, 42, 0).normalize());
	}

	@Test
	public void conjugate() {
		assertEquals(new Quaternion(quaternion.w, 0, -quaternion.y, 0), quaternion.conjugate());
	}

	@Test
	public void toRotation() {
		final Rotation rot = Rotation.of(Vector.Y_AXIS, MathsUtil.PI);
		assertEquals(rot, quaternion.toRotation());
	}

	@Test
	public void multiply() {
		assertEquals(new Quaternion(-1, 0, 0, 0), quaternion.multiply(quaternion));
	}

	@Test
	public void rotatePoint() {
		final Point pt = new Point(1, 0, 0);
		assertEquals(new Point(-1, 0, 0), quaternion.rotate(pt));
	}

	@Test
	public void equals() {
		assertEquals(true, quaternion.equals(quaternion));
		assertEquals(true, quaternion.equals(Quaternion.of(Rotation.of(Vector.Y_AXIS, MathsUtil.PI))));
		assertEquals(false, quaternion.equals(null));
		assertEquals(false, quaternion.equals(Quaternion.of(Rotation.of(Vector.X_AXIS, MathsUtil.PI))));
	}
}
