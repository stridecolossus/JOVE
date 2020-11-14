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
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		assertEquals(Vector.Y_AXIS, cam.up());
		assertEquals(Vector.X_AXIS, cam.right());
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
		cam.move(vec);
		assertEquals(new Point(vec.scale(2)), cam.position());
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
		cam.direction(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, cam.direction());
	}

	@Test
	void right() {
		cam.direction(Vector.X_AXIS);
		cam.matrix();
		assertEquals(Vector.Z_AXIS, cam.right());
	}

	@Test
	void look() {
		cam.move(new Point(0, 0, 1));
		cam.look(Point.ORIGIN);
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
	}

	@Test
	void lookInvalid() {
		assertThrows(IllegalArgumentException.class, () -> cam.look(Point.ORIGIN));
	}

	@Test
	void up() {
		cam.up(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, cam.up());
	}

	@Test
	void matrix() {
		// Create camera rotation
		final Matrix rot = new Matrix.Builder()
				.identity()
				.row(0, Vector.X_AXIS)
				.row(1, Vector.Y_AXIS)
				.row(2, Vector.Z_AXIS)
				.build();

		// Create camera translation one unit out of the screen
		final Point pos = new Point(0, 0, 1);
		final Matrix trans = Matrix.translation(new Vector(pos).invert());

		// Init camera and check matrix
		cam.move(pos);
		cam.look(Point.ORIGIN);
		assertEquals(rot.multiply(trans), cam.matrix());
	}
}
