package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.Trigonometric.PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.Cosine;

class AxisAngleTest {
	private AxisAngle rot;

	@BeforeEach
	void before() {
		rot = new AxisAngle(Y, PI);
	}

	@Test
	void constructor() {
		assertEquals(Y, rot.axis());
		assertEquals(PI, rot.angle());
	}

	@Test
	void cosine() {
		assertEquals(Cosine.DEFAULT, rot.cosine());
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(PI, Cosine.DEFAULT), rot.matrix());
	}

	@Test
	void custom() {
		final Cosine cosine = mock(Cosine.class);
		rot = AxisAngle.of(rot, cosine);
		assertEquals(cosine, rot.cosine());
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, new AxisAngle(Y, PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, new AxisAngle(Y, 0));
	}
}
