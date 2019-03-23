package org.sarge.jove.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class LightTest {
	@Test
	public void ambient() {
		final Light light = Light.ambient(Colour.WHITE);
		assertEquals(Colour.WHITE, light.colour());
		assertEquals(null, light.position());
		assertEquals(null, light.direction());
		assertEquals(1, light.properties().size());
	}

	@Test
	public void directional() {
		final Light light = Light.directional(Colour.WHITE, Vector.X_AXIS);
		assertEquals(Colour.WHITE, light.colour());
		assertEquals(null, light.position());
		assertEquals(Vector.X_AXIS, light.direction());
		assertEquals(2, light.properties().size());
	}

	@Test
	public void point() {
		final Light light = Light.point(Colour.WHITE, Point.ORIGIN);
		assertEquals(Colour.WHITE, light.colour());
		assertEquals(Point.ORIGIN, light.position());
		assertEquals(null, light.direction());
		assertEquals(2, light.properties().size());
	}

	@Test
	public void spotlight() {
		final Light light = Light.spotlight(Colour.WHITE, Point.ORIGIN, Vector.X_AXIS);
		assertEquals(Colour.WHITE, light.colour());
		assertEquals(Point.ORIGIN, light.position());
		assertEquals(Vector.X_AXIS, light.direction());
		assertEquals(3, light.properties().size());
	}

	@Nested
	class MutatorTests {
		private Light ambient;
		private Light spotlight;

		@BeforeEach
		public void before() {
			ambient = Light.ambient(Colour.WHITE);
			spotlight = Light.spotlight(Colour.WHITE, Point.ORIGIN, Vector.X_AXIS);
		}

		@Test
		public void colour() {
			ambient.colour(Colour.BLACK);
			assertEquals(Colour.BLACK, ambient.colour());
		}

		@Test
		public void position() {
			final Point pos = new Point(1, 2, 3);
			spotlight.position(pos);
			assertEquals(pos, spotlight.position());
		}

		@Test
		public void positionInvalid() {
			assertThrows(IllegalArgumentException.class, () -> ambient.position(Point.ORIGIN));
		}

		@Test
		public void direction() {
			spotlight.direction(Vector.Y_AXIS);
			assertEquals(Vector.Y_AXIS, spotlight.direction());
		}

		@Test
		public void directionInvalid() {
			assertThrows(IllegalArgumentException.class, () -> ambient.direction(Vector.Y_AXIS));
		}
	}
}
