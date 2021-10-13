package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

class CameraTest {
	private Camera cam;

	@BeforeEach
	void before() {
		cam = new Camera();
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, cam.position());
		assertEquals(Vector.Z.negate(), cam.direction());
		assertEquals(Vector.Y, cam.up());
		assertEquals(Vector.X, cam.right());
		assertNotNull(cam.matrix());
	}

	@Test
	void move() {
		final Point pos = new Point(1, 2, 3);
		cam.move(pos);
		assertEquals(pos, cam.position());
	}

	@Test
	void moveVector() {
		final Vector vec = new Vector(1, 2, 3);
		cam.move(vec);
		assertEquals(new Point(vec), cam.position());
	}

	@Test
	void moveDistance() {
		cam.move(3);
		assertEquals(new Point(0, 0, 3), cam.position());
	}

	@Test
	void strafe() {
		cam.strafe(3);
		assertEquals(new Point(3, 0, 0), cam.position());
	}

	@Test
	void direction() {
		cam.direction(Vector.X);
		assertEquals(Vector.X, cam.direction());
	}

	@Test
	void right() {
		cam.direction(Vector.X);
		cam.matrix();
		assertEquals(Vector.Z, cam.right());
	}

	@Test
	void look() {
		cam.move(new Point(0, 0, 1));
		cam.look(Point.ORIGIN);
		assertEquals(Vector.Z.negate(), cam.direction());
	}

	@Test
	void lookInvalid() {
		assertThrows(IllegalArgumentException.class, () -> cam.look(Point.ORIGIN));
	}

	@Test
	void up() {
		cam.up(Vector.X);
		assertEquals(Vector.X, cam.up());
	}

	@Test
	void matrix() {
		// Create camera rotation
		final Matrix rot = new Matrix.Builder()
				.identity()
				.row(0, Vector.X)
				.row(1, Vector.Y)
				.row(2, Vector.Z)
				.build();

		// Create camera translation one unit out of the screen
		final Matrix trans = Matrix.translation(Vector.Z.negate());

		// Init camera and check matrix
		cam.move(new Point(0, 0, 1));
		cam.look(Point.ORIGIN);
		assertEquals(rot.multiply(trans), cam.matrix());
	}
}
