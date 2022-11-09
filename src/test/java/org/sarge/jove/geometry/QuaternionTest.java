package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.Trigonometric.PI;

import org.junit.jupiter.api.*;

public class QuaternionTest {
	private Quaternion quaternion;

	@BeforeEach
	void before() {
		quaternion = new Quaternion(0, 0, 1, 0);
	}

	@Test
	void constructor() {
		assertEquals(0, quaternion.w);
		assertEquals(0, quaternion.x);
		assertEquals(1, quaternion.y);
		assertEquals(0, quaternion.z);
	}

	@Test
	void axis() {
		assertEquals(quaternion, new Quaternion(0, Axis.Y));
	}

	@Test
	void of() {
		assertEquals(quaternion, Quaternion.of(new AxisAngle(Y, PI)));
	}

	@Test
	void magnitude() {
		assertEquals(1, quaternion.magnitude());
	}

	@Test
	void array() {
		assertArrayEquals(new float[]{0, 0, 1, 0}, quaternion.array());
	}

	@Test
	void matrix() {
		final Rotation rot = new AxisAngle(Y, PI);
		assertEquals(rot.matrix(), quaternion.matrix());
	}

	@Test
	void normalize() {
		assertEquals(quaternion, quaternion.normalize());
		assertEquals(quaternion, new Quaternion(0, 0, 42, 0).normalize());
	}

	@Test
	void conjugate() {
		assertEquals(new Quaternion(0, 0, -1, 0), quaternion.conjugate());
	}

	@Test
	void rotation() {
		assertEquals(new AxisAngle(Y, PI), quaternion.toAxisAngle());
	}

	@Test
	void rotate() {
		final Vector vec = new Vector(1, 0, 0);
		assertEquals(new Vector(-1, 0, 0), quaternion.rotate(vec));
	}

	@Test
	void multiply() {
		assertEquals(new Quaternion(-1, 0, 0, 0), quaternion.multiply(quaternion));
	}

	@Test
	void equals() {
		assertEquals(true, quaternion.equals(quaternion));
		assertEquals(true, quaternion.equals(new Quaternion(0, 0, 1, 0)));
		assertEquals(false, quaternion.equals(null));
		assertEquals(false, quaternion.equals(new Quaternion(0, 1, 0, 0)));
	}
}
