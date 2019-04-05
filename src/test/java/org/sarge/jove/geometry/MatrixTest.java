package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.util.MathsUtil;

public class MatrixTest {
	private Matrix matrix;
	private Matrix identity;

	@BeforeEach
	public void before() {
		matrix = new Matrix(new float[]{1, 2, 3, 4}); // TODO - should array ctor have row/col major boolean?
		identity = Matrix.identity(2);
	}

	@Test
	public void constructor() {
		assertEquals(2, matrix.order());
		assertFloatEquals(1, matrix.get(0, 0));
		assertFloatEquals(2, matrix.get(1, 0));
		assertFloatEquals(3, matrix.get(0, 1));
		assertFloatEquals(4, matrix.get(1, 1));
		assertEquals(matrix, matrix.matrix());
		assertEquals(2 * 2, matrix.size());
	}

	@Test
	public void constructorInvalidArrayDimensions() {
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[]{1, 2, 3}));
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[]{}));
	}

	@Test
	public void buffer() {
		final FloatBuffer buffer = FloatBuffer.allocate(2 * 2);
		matrix.buffer(buffer);
		buffer.flip();
		for(int n = 0; n < 4; ++n) {
			assertFloatEquals(n + 1, buffer.get());
		}
	}

	@Test
	public void transpose() {
		assertEquals(new Matrix(new float[]{1, 3, 2, 4}), matrix.transpose());
		assertEquals(identity, identity.transpose());
	}

	@Test
	public void multiply() {
		final float[] expected = {
			1 * 1 + 3 * 2,
			2 * 1 + 4 * 2,
			1 * 3 + 3 * 4,
			2 * 3 + 4 * 4,
		};
		assertEquals(new Matrix(expected), matrix.multiply(matrix));
	}

	@Test
	public void multiplyIndentity() {
		assertEquals(matrix, matrix.multiply(identity));
		assertEquals(matrix, identity.multiply(matrix));
	}

	//@Test
	public void multiplyPoint() {
		matrix = new Matrix.Builder()
			.identity()
			.set(0, 1, 2)
			.set(0, 2, 3)
			.set(0, 3, 4)
			.build();
		final Point pos = new Point(1, 2, 3);
		assertEquals(new Point((1 * 1) + (2 * 2) + (3 * 3) + (1 * 4), 2, 3), matrix.multiply(pos));
	}

	//@Test
	public void multiplyIdentityPoint() {
		final Point pos = new Point(1, 2, 3);
		assertEquals(pos, Matrix.IDENTITY.multiply(pos));
	}

	// TODO - REMOVE
	// https://github.com/JOML-CI/JOML/blob/master/src/org/joml/Matrix4f.java
	// private Matrix4f perspectiveGeneric(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne, Matrix4f dest) {
	@Test
	public void test() {
		//final Matrix projection = Projection.DEFAULT.matrix(0.01f, 100f, new Dimensions(640, 480));
		final Matrix projection = Projection.perspective(MathsUtil.toRadians(60)).matrix(0.1f, 256f, new Dimensions(1280, 720));
		System.out.println(projection);

		final Camera cam = new Camera();
		cam.move(new Point(0, 0, -1));
		System.out.println(cam.matrix());

		//final Matrix pmv = projection.multiply(cam.matrix());
		//final Matrix pmv = cam.matrix().multiply(projection);
		//System.out.println(pmv);
		//System.out.println(cam.matrix().multiply(projection));

//		final Matrix pmv = new Matrix.Builder()
//			.identity()
//			.set(3, 2, -1f / 2)
//			.build();

		final float z = 0.25f;
		final Point[] pts = new Point[] {
			new Point(-0.5f, -0.5f, z),
			new Point(-0.5f, +0.5f, z),
			new Point(+0.5f, -0.5f, z),
			new Point(+0.5f, +0.5f, z),
		};
		for(int n = 0; n < pts.length; ++n) {
			final Point result = cam.matrix().multiply(pts[n]);
			projection.multiply(result);
//			final Point result = projection.multiply(pts[n]);
//			cam.matrix().multiply(result);
			System.out.println();
		}
	}

	@Test
	public void equals() {
		assertTrue(matrix.equals(matrix));
		assertTrue(matrix.equals(new Matrix(new float[]{1, 2, 3, 4})));
		assertFalse(matrix.equals(null));
		assertFalse(matrix.equals(new Matrix(new float[]{1})));
		assertFalse(matrix.equals(new Matrix(new float[]{1, 2, 3, 999})));
	}

	@Nested
	class FactoryMethods {
		@Test
		public void identity() {
			assertEquals(new Matrix(new float[]{1, 0, 0, 1}), Matrix.identity(2));
		}

		@Test
		public void translation() {
			final Matrix expected = new Builder().identity().column(3, Vector.X_AXIS).build();
			assertEquals(expected, Matrix.translation(Vector.X_AXIS));
		}

		@Test
		public void scale() {
			final Matrix expected = new Builder().identity().set(2, 2, 3).build();
			assertEquals(expected, Matrix.scale(new Tuple(1, 1, 3)));
		}

		@Test
		public void rotation() {
			final Matrix expected = new Builder()
				.identity()
				.set(1, 1, MathsUtil.cos(MathsUtil.HALF))
				.set(1, 2, MathsUtil.sin(MathsUtil.HALF))
				.set(2, 1, -MathsUtil.sin(MathsUtil.HALF))
				.set(2, 2, MathsUtil.cos(MathsUtil.HALF))
				.build();
			assertEquals(expected, Matrix.rotation(Vector.X_AXIS, MathsUtil.HALF));
		}

		@Test
		public void rotationInvalidAxis() {
			assertThrows(UnsupportedOperationException.class, () -> Matrix.rotation(new Vector(1, 2, 3), MathsUtil.HALF));
		}
	}

	@Nested
	class BuilderTest {
		@Test
		public void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}

		@Test
		public void identity() {
			final Matrix result = new Builder(2).identity().build();
			assertEquals(new Matrix(new float[]{1, 0, 0, 1}), result);
		}

		@Test
		public void set() {
			final Matrix result = new Builder(2).set(0, 1, 2).build();
			assertEquals(new Matrix(new float[]{0, 0, 2, 0}), result);
		}

		@Test
		public void row() {
			final Matrix result = new Builder(3).row(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(new float[]{0, 1, 0, 0, 2, 0, 0, 3, 0}), result);
		}

		@Test
		public void column() {
			final Matrix result = new Builder(3).column(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(new float[]{0, 0, 0, 1, 2, 3, 0, 0, 0}), result);
		}
	}
}
