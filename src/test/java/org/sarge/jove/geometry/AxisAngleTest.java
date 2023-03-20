package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
		assertEquals(Cosine.DEFAULT, rot.cosine());
	}

	@Test
	void rotation() {
		assertEquals(rot, rot.toAxisAngle());
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(PI), rot.matrix());
	}

	@Test
	void of() {
		final Cosine cos = mock(Cosine.class);
		rot = rot.of(cos);
		assertEquals(cos, rot.cosine());
		rot.matrix();
		verify(cos).cos(PI);
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, new AxisAngle(Y, PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, new AxisAngle(Y, 0));
	}
}
