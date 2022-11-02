package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtil;

class AbstractAxisAngleTest {
	private AbstractAxisAngle rot;

	@BeforeEach
	void before() {
		rot = new AbstractAxisAngle(Axis.Y) {
			@Override
			public Matrix matrix() {
				return null;
			}

			@Override
			public float angle() {
				return MathsUtil.PI;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Axis.Y, rot.axis());
		assertSame(rot, rot.toAxisAngle());
	}

	@Test
	void rotate() {
		final Vector vec = new Vector(1, 1, 0).normalize();
		assertEquals(new Vector(-1, 1, 0).normalize(), rot.rotate(vec));
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, AxisAngle.of(Axis.Y, MathsUtil.PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, AxisAngle.of(Axis.Y, 0));
	}
}
