package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.volume.*;

class CapsuleTest {
	private Capsule capsule;
	private Point top, bottom;

	@BeforeEach
	void before() {
		top = new Point(0, 4, 0);
		bottom = new Point(0, 1, 0);
		capsule = new Capsule(top, bottom, 2);
	}

	@Test
	void constructor() {
		assertEquals(2, capsule.radius());
	}

	@DisplayName("A capsule TODO")
	@Test
	void bounds() {
		final Bounds expected = new Bounds(top, bottom);
		assertEquals(expected, capsule.bounds());
	}

	@DisplayName("A capsule...")
	@Nested
	class ContainTests {
		@DisplayName("contains the top and bottom points")
		@Test
		void ends() {
			assertEquals(true, capsule.contains(top));
			assertEquals(true, capsule.contains(bottom));
		}

		@DisplayName("contains points that lie on the line segment")
		@Test
		void segment() {
			assertEquals(true, capsule.contains(new Point(0, 3, 0)));
		}

		@DisplayName("does not contain points above the top point")
		@Test
		void above() {
			assertEquals(false, capsule.contains(new Point(0, 5, 0)));
		}

		@DisplayName("does not contain points below the bottom point")
		@Test
		void below() {
			assertEquals(false, capsule.contains(new Point(0, 0, 0)));
		}

		@DisplayName("does not contain points outside the capsule radius")
		@Test
		void outside() {
			assertEquals(false, capsule.contains(new Point(0, 2, 3)));
		}
	}
}
