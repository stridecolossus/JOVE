package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

class CameraTest {
	private Camera camera;

	@BeforeEach
	void before() {
		camera = new Camera();
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, camera.position());
		assertEquals(X, camera.right());
		assertEquals(Y, camera.up());
		assertEquals(Z, camera.direction());
		assertEquals(MathsUtility.toRadians(60), camera.fov());
	}

	@DisplayName("The view matrix of a new camera is the identity matrix")
	@Test
	void matrix() {
		assertEquals(Matrix.IDENTITY, camera.matrix());
	}

	@Test
	void fov() {
		camera.fov(MathsUtility.HALF_PI);
		assertEquals(MathsUtility.HALF_PI, camera.fov());
	}

	@DisplayName("The camera...")
	@Nested
	class PositionTest {
		@DisplayName("can be moved to a new location")
		@Test
		void move() {
			final Point pos = new Point(1, 2, 3);
			camera.move(pos);
			assertEquals(pos, camera.position());
			assertEquals(X, camera.right());
			assertEquals(Y, camera.up());
			assertEquals(Z, camera.direction());
		}

		@DisplayName("can be moved relative to its current location")
		@Test
		void vector() {
			final Vector vec = new Vector(1, 2, 3);
			camera.move(vec);
			assertEquals(new Point(vec), camera.position());
		}

		@DisplayName("can be moved along the current view direction")
		@Test
		void distance() {
			camera.move(3);
			assertEquals(new Point(0, 0, 3), camera.position());
		}

		@DisplayName("can be strafed along the right axis of the camera")
		@Test
		void strafe() {
			camera.strafe(3);
			assertEquals(new Point(3, 0, 0), camera.position());
		}

		@DisplayName("updates the translation component of the view matrix when it is moved")
		@Test
		void translation() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.column(3, new Vector(-1, -2, -3))
					.build();

			camera.move(new Point(1, 2, 3));
			assertEquals(expected, camera.matrix());
		}
	}

	@Nested
	class AxesTest {
		@DisplayName("The camera can be pointed in a given direction")
		@Test
		void direction() {
			final Normal dir = new Normal(3, 4, 5);
			camera.direction(dir);
			assertEquals(dir, camera.direction());
			assertEquals(Y, camera.up());
			assertEquals(new Normal(Y.cross(dir)), camera.right());
		}

		@DisplayName("The up axis of the camera can be configured")
		@Test
		void up() {
			camera.up(X);
			assertEquals(X, camera.up());
			assertEquals(Y.invert(), camera.right());
		}

		@DisplayName("The rotational component of the view matrix is recalculated if the camera axes are modified")
		@Test
		void rotation() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.row(0, new Vector(0, 0, -1))
					.row(1, new Vector(-0.8f, 0.6f, 0))
					.row(2, new Vector(0.6f, 0.8f, 0))
					.build();

			camera.direction(new Normal(3, 4, 0));
			assertEquals(expected, camera.matrix());
		}
	}

	@Nested
	class LookTest {
		@DisplayName("The camera can be pointed at a target location")
		@Test
		void look() {
			camera.move(new Point(3, 4, 0));
			camera.look(Point.ORIGIN);
			assertEquals(Z.invert(), camera.right());
			assertEquals(Y, camera.up());
			assertEquals(new Normal(0.6f, 0.8f, 0), camera.direction());
		}

		@DisplayName("The camera view matrix is recalculated when it is pointed at a target location")
		@Test
		void matrix() {
			// Build the 3x3 rotation component
			final Matrix rotation = new Matrix.Builder()
					.identity()
					.column(0, new Vector(0, 0, -1))
					.column(1, new Vector(-0.8f, 0.6f, 0))
					.column(2, new Vector(0.6f, 0.8f, 0))
					.build()
					.transpose();

			// Build the translation component
			final var translation = Matrix.translation(new Vector(0, 0, -5));

			// Build expected view matrix
			final Matrix expected = translation.multiply(rotation);

			// Check resultant view matrix
			camera.move(new Point(3, 4, 0));
			camera.look(Point.ORIGIN);
			assertEquals(expected, camera.matrix());
		}

		@DisplayName("The camera cannot be pointed at its current location")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> camera.look(Point.ORIGIN));
		}
	}
}
