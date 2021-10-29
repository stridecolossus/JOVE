package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	public void of() {
		assertEquals(quaternion, Quaternion.of(Vector.Y, PI));
	}

	@Test
	public void magnitude() {
		assertEquals(1, quaternion.magnitude());
	}

	@Test
	public void matrix() {
		assertEquals(Rotation.matrix(Vector.Y, PI), quaternion.matrix());
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
		final Rotation rot = quaternion.rotation();
		assertNotNull(rot);
		assertEquals(Vector.Y, rot.axis());
		assertEquals(PI, rot.angle());
		assertEquals(quaternion.matrix(), rot.matrix());
	}

	@Test
	public void multiply() {
		assertEquals(new Quaternion(-1, 0, 0, 0), quaternion.multiply(quaternion));
	}

	@Test
	public void rotate() {
		final Vector vec = new Vector(1, 0, 0);
		assertEquals(new Vector(-1, 0, 0), quaternion.rotate(vec));
	}

	@Test
	public void equals() {
		assertEquals(true, quaternion.equals(quaternion));
		assertEquals(true, quaternion.equals(Quaternion.of(Vector.Y, PI)));
		assertEquals(false, quaternion.equals(null));
		assertEquals(false, quaternion.equals(Quaternion.of(Vector.X, PI)));
	}
}
