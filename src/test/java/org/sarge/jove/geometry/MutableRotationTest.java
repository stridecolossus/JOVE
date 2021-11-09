package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sarge.jove.util.MathsUtil.HALF;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MutableRotationTest {
	private MutableRotation rot;

	@BeforeEach
	void before() {
		rot = new MutableRotation(Vector.Y);
	}

	@Test
	void constructor() {
		assertEquals(0, rot.angle());
		assertEquals(true, rot.isDirty());
	}

	@Test
	void constructorArbitraryAxis() {
		final Vector axis = new Vector(1, 2, 3).normalize();
		rot = new MutableRotation(axis);
		assertEquals(axis, rot.axis());
		assertNotNull(rot.matrix());
	}

	@Test
	void matrix() {
		assertEquals(Rotation.matrix(Vector.Y, 0), rot.matrix());
		assertEquals(false, rot.isDirty());
	}

	@Test
	void angle() {
		rot.angle(HALF);
		assertEquals(Rotation.matrix(Vector.Y, HALF), rot.matrix());
	}

	@Test
	void equals() {
		assertEquals(true, rot.equals(rot));
		assertEquals(true, rot.equals(new MutableRotation(Vector.Y)));
		assertEquals(false, rot.equals(null));
		assertEquals(false, rot.equals(new MutableRotation(Vector.Z)));
	}
}

