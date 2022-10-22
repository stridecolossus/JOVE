package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

class CameraTest {
	private Camera cam;

	@BeforeEach
	void before() {
		cam = new Camera();
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, cam.position());
		assertEquals(X.vector(), cam.right());
		assertEquals(Y.vector(), cam.up());
		assertEquals(Z.vector(), cam.direction());
		assertNotNull(cam.matrix());
	}

	@Test
	void move() {
		final Point pos = new Point(1, 2, 3);
		cam.move(pos);
		assertEquals(pos, cam.position());
		assertEquals(X.vector(), cam.right());
		assertEquals(Y.vector(), cam.up());
		assertEquals(Z.vector(), cam.direction());
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
		final Vector dir = new Vector(1, 1, 0).normalize();
		cam.direction(dir);
		cam.update();
		assertEquals(dir, cam.direction());
		assertEquals(Y.vector(), cam.up());
		assertEquals(Y.vector().cross(dir).normalize(), cam.right());
	}

	@Test
	void directionLock() {
		assertThrows(IllegalStateException.class, () -> cam.direction(Y.vector()));
		assertThrows(IllegalStateException.class, () -> cam.direction(Y.vector().invert()));
	}

	@Test
	void look() {
		cam.look(new Point(0, 0, -1));
		cam.update();
		assertEquals(X.vector(), cam.right());
		assertEquals(Y.vector(), cam.up());
		assertEquals(Z.vector(), cam.direction());
	}

	@Test
	void lookInvalid() {
		assertThrows(IllegalArgumentException.class, () -> cam.look(Point.ORIGIN));
	}

	@Test
	void lookLock() {
		assertThrows(IllegalStateException.class, () -> cam.look(new Point(Y.vector())));
		assertThrows(IllegalStateException.class, () -> cam.look(new Point(Y.vector().invert())));
	}

	@Test
	void up() {
		cam.up(X.vector());
		assertEquals(X.vector(), cam.up());
	}

	@Test
	void matrix() {
		// Create camera rotation
		final Matrix rot = new Matrix.Builder()
				.identity()
				.row(0, X.vector())
				.row(1, Y.vector())
				.row(2, Z.vector())
				.build();

		// Create camera translation one unit out of the screen
		final Matrix trans = Matrix4.translation(new Vector(0, 0, -1));

		// Init camera and check matrix
		cam.move(new Point(0, 0, 1));
		cam.look(Point.ORIGIN);
		assertEquals(rot.multiply(trans), cam.matrix());
	}
}
