package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

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
		assertEquals(Matrix.IDENTITY, cam.matrix());
	}

	@Test
	void move() {
		final Point pos = new Point(1, 2, 3);
		cam.move(pos);
		assertEquals(pos, cam.position());
		check();
	}

	@Test
	void moveVector() {
		final Vector vec = new Vector(1, 2, 3);
		cam.move(vec);
		cam.move(vec);
		assertEquals(new Point(vec.scale(2)), cam.position());
		check();
	}

	@Test
	void moveDistance() {
		cam.move(3);
		assertEquals(new Point(0, 0, 3), cam.position());
		check();
	}

	@Test
	void strafe() {
		cam.strafe(3);
		assertEquals(new Point(3, 0, 0), cam.position());
		check();
	}

	@Test
	void direction() {
		cam.direction(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, cam.direction());
		assertEquals(Vector.Z_AXIS, cam.right());
		check();
	}

	@Test
	void look() {
		cam.move(new Point(0, 0, -1));
		cam.look(Point.ORIGIN);
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		check();
	}

	@Test
	void orientation() {
		final float angle = MathsUtil.toRadians(45);
		cam.orientation(angle, angle);
		final float cos = (float) Math.cos(angle);
		final float x = cos * cos;
		final float y = (float) Math.sin(angle);
		final float z = (float) Math.sin(angle) * cos;
		assertEquals(new Vector(x, y, -z).normalize(), cam.direction());
		check();
	}

	@Test
	void orientationIdentity() {
		cam.orientation(MathsUtil.HALF_PI, 0);
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		check();
	}

//	@Test
//	void rotate() {
//		cam.rotate(Rotation.of(cam.right(), -MathsUtil.HALF_PI));
//		assertEquals(new Vector(0, -1, 0), cam.direction());
//		check();
//	}

	@Test
	void up() {
		cam.up(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, cam.up());
	}

	private void check() {
		// Update matrix (and camera orientation)
		final Matrix actual = cam.matrix();
		final Vector x = cam.direction().cross(Vector.Y_AXIS).normalize();

		// Build orientation matrix
		final Matrix rot = new Matrix.Builder()
				.identity()
				.row(0, x)
				.row(1, x.cross(cam.direction()).normalize())
				.row(2, cam.direction().invert())
				.build();

		// Build translation matrix
		final Matrix trans = new Matrix.Builder()
				.identity()
				.set(0, 3, cam.position().x)
				.set(1, 3, cam.position().y)
				.set(2, 3, cam.position().z)
				.build();

		// Check resultant matrix
		assertEquals(rot.multiply(trans), actual);
	}
}
