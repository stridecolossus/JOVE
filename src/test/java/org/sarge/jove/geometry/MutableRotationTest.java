package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtil.HALF;

import org.junit.jupiter.api.*;

public class MutableRotationTest {
	private MutableRotation rot;

	@BeforeEach
	void before() {
		rot = new MutableRotation(Vector.Y);
	}

	@Test
	void constructor() {
		assertEquals(Vector.Y, rot.axis());
		assertEquals(0, rot.angle());
		assertEquals(true, rot.isDirty());
	}

	@Test
	void matrix() {
		assertEquals(Quaternion.of(rot).matrix(), rot.matrix());
		assertEquals(false, rot.isDirty());
	}

	@Test
	void angle() {
		rot.angle(HALF);
		assertEquals(HALF, rot.angle());
		assertNotNull(rot.matrix());
	}
}
