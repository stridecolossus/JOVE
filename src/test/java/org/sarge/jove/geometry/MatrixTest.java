package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.MathsUtil;

// TODO - expand to tests for 2x2, 3x3 (?) and 4x4
public class MatrixTest {
	private Matrix matrix;

	@Before
	public void before() {
		matrix = new Matrix(new float[][]{ { 1, 2 }, { 3, 4 } });
	}

	@Test
	public void identityConstructor() {
		matrix = new Matrix(4);
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				if(r == c) {
					assertFloatEquals(1, matrix.get(r, c));
				}
				else {
					assertFloatEquals(0, matrix.get(r, c));
				}
			}
		}
	}

	@Test
	public void getOrder() {
		assertEquals(2, matrix.getOrder());
	}

	@Test
	public void get() {
		assertFloatEquals(1, matrix.get(0, 0));
		assertFloatEquals(2, matrix.get(0, 1));
		assertFloatEquals(3, matrix.get(1, 0));
		assertFloatEquals(4, matrix.get(1, 1));
	}

	@Test
	public void transpose() {
		final Matrix transpose = new Matrix(new float[][]{ { 1, 3 }, { 2, 4 } });
		assertEquals(transpose, matrix.transpose());
	}

	@Test
	public void multiplyScale() {
		final Matrix scaled = new Matrix(new float[][]{ { 3, 6 }, { 9, 12 } });
		assertEquals(scaled, matrix.multiply(3));
	}

	@Test
	public void add() {
		final Matrix result = matrix.add(matrix);
		assertEquals(matrix.multiply(2), result);
	}

	@Test
	public void multiply() {
		final Matrix result = matrix.multiply(matrix);
		final Matrix expected = new Matrix(new float[][]{ { 7, 10 }, { 15, 22 } });
		assertEquals(expected, result);
	}

	@Test
	public void getDeterminant() {
		assertFloatEquals((1 * 4) - (3 * 2), matrix.getDeterminant());
	}

	@Test
	public void invert() {
		// TODO
		//assertEquals( new Matrix( 2 ), matrix.invert() );
	}

	@Test
	public void multiplyPoint() {
		matrix = Matrix.rotation(new Rotation(Vector.Y_AXIS, MathsUtil.HALF_PI));
		final Point pt = new Point(1, 2, 3);
		final Point result = matrix.multiply(pt);
		assertEquals(new Vector(3, 2, -1), result);
		assertEquals(result, pt);
	}

	@Test
	public void getSubMatrix() {
		matrix = new Matrix(4);
		final Matrix sub = matrix.getSubMatrix(3);
		assertNotNull(sub);
		assertEquals(3, sub.getOrder());
	}

	@Test
	public void getComponentSize() {
		assertEquals(2, matrix.getComponentSize());
	}

	@Test
	public void append() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer(4);
		matrix.append(buffer);
		buffer.flip();
		assertFloatEquals(1, buffer.get());
		assertFloatEquals(3, buffer.get());
		assertFloatEquals(2, buffer.get());
		assertFloatEquals(4, buffer.get());
	}

	@Test
	public void equals() {
		assertEquals(matrix, matrix);
		assertTrue(matrix.equals(matrix));
		assertFalse(matrix.equals(false));
		assertFalse(matrix.equals(new Matrix(2)));
	}

	@Test
	public void translation() {
		final Vector trans = new Vector(1, 2, 3);
		final MatrixBuilder expected = new MatrixBuilder(4);
		expected.set(0, 3, trans.x);
		expected.set(1, 3, trans.y);
		expected.set(2, 3, trans.z);
		assertEquals(expected, Matrix.translation(trans));
	}

	@Test
	public void rotation() {
		final Rotation rot = new Rotation(Vector.Y_AXIS, MathsUtil.PI);
		final Quaternion q = new Quaternion(rot);
		final Matrix m = q.toMatrix();
		assertEquals(m, Matrix.rotation(rot));
	}

	@Test
	public void scale() {
		final MatrixBuilder m = new MatrixBuilder(4);
		m.set(0, 0, 4);
		m.set(1, 1, 5);
		m.set(2, 2, 6);
		assertEquals(m, Matrix.scale(4, 5, 6));
	}
}
