package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.Trigonometric.HALF_PI;

import org.junit.jupiter.api.*;

public class MutableRotationTest {
	private MutableRotation rot;

	@BeforeEach
	void before() {
		rot = new MutableRotation(Y);
	}

	@Test
	void constructor() {
		assertEquals(0, rot.angle());
		assertEquals(true, rot.isMutable());
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(0), rot.matrix());
	}

	@Test
	void set() {
		rot.set(HALF_PI);
		assertEquals(HALF_PI, rot.angle());
		assertEquals(Y.rotation(HALF_PI), rot.matrix());
	}
}
