package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.Shader;

public class CameraTest {
	private Camera cam;

	@BeforeEach
	public void before() {
		cam = new Camera();
	}

	@Test
	public void constructor() {
		assertEquals(Point.ORIGIN, cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		assertEquals(Vector.Y_AXIS, cam.up());
		assertEquals(Vector.X_AXIS, cam.right());
		assertNotNull(cam.matrix());
		assertEquals(false, cam.isDirty());
	}

	@Test
	public void matrix() {
		cam.move(new Point(0, 0, -5));
		cam.point(Vector.X_AXIS);
		final Matrix matrix = new Matrix.Builder()
			.identity()
			.set(0, 0, 0)
			.set(0, 2, 1)
			.set(0, 3, 5)
			.set(2, 0, -1)
			.set(2, 2, 0)
			.build();
		assertEquals(matrix, cam.matrix());
		assertEquals(false, cam.isDirty());
	}

	@Test
	public void movePosition() {
		final Point pos = new Point(1, 2, 3);
		cam.move(pos);
		assertEquals(pos, cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		assertEquals(true, cam.isDirty());
	}

	@Test
	public void moveVector() {
		final Vector vec = new Vector(1, 2, 3);
		cam.move(vec);
		cam.move(vec);
		assertEquals(new Point(vec.scale(2)), cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		assertEquals(true, cam.isDirty());
	}

	@Test
	public void moveDistance() {
		cam.move(3);
		assertEquals(new Point(0, 0, -3), cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		assertEquals(true, cam.isDirty());
	}

	@Test
	public void point() {
		cam.point(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
		assertEquals(true, cam.isDirty());
	}

	@Test
	public void look() {
		cam.look(new Point(0, 1, 0));
		assertEquals(Vector.Y_AXIS, cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
		assertEquals(true, cam.isDirty());
	}

	@Test
	public void up() {
		cam.up(Vector.X_AXIS);
		assertEquals(true, cam.isDirty());
		cam.matrix();
		assertEquals(Vector.X_AXIS, cam.up());
		assertEquals(new Vector(0, -1, 0), cam.right());
	}

	@Nested
	class PropertyTests {
		@Test
		public void viewMatrixProperty() {
			final Material.Property prop = cam.viewMatrixProperty();
			assertNotNull(prop);
			assertEquals(4 * 4, prop.binder().size());
			check(prop, cam.matrix());
		}

		@Test
		public void positionProperty() {
			final Material.Property prop = cam.positionProperty();
			assertNotNull(prop);
			assertEquals(3, prop.binder().size());
			check(prop, cam.position());
		}

		@Test
		public void directionProperty() {
			final Material.Property prop = cam.directionProperty();
			assertNotNull(prop);
			assertEquals(3, prop.binder().size());
			check(prop, cam.direction());
		}
	}

	private static void check(Material.Property prop, Bufferable bufferable) {
		final Shader.Parameter param = mock(Shader.Parameter.class);
		prop.binder().apply(param);
		// TODO
	}
}
