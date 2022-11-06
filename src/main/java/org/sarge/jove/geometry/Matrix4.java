package org.sarge.jove.geometry;

import org.sarge.jove.common.Component;

/**
 * Standard 4x4 matrix used for transformation and projection.
 * <p>
 * This class provides various convenience constants and factory methods for common transformations.
 * <p>
 * The matrix is assumed to have the following structure for view and perspective transformation:
 * <p>
 * <table border=0>
 * <tr><td>Rx</td><td>Ry</td><td>Rz</td><td>Tx</td></tr>
 * <tr><td>Yx</td><td>Yy</td><td>Yz</td><td>Ty</td></tr>
 * <tr><td>Dx</td><td>Dy</td><td>Dz</td><td>Tz</td></tr>
 * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
 * </table>
 * <p>
 * Where:
 * <ul>
 * <li>The top-left 3x3 component of the matrix is the view rotation</li>
 * <li>The right-hand column is the transformation</li>
 * <li>R is the <i>right</i> vector, Y is <i>up</i> and D is the view <i>direction</i> (in camera terms)</li>
 * <li>T is the view transformation (or eye position)</li>
 * </ul>
 * Note that both components are inverted (transposed and negated) since the scene is transformed in the opposite direction to the view (or camera).
 * <p>
 * @author Sarge
 */
public class Matrix4 extends Matrix {
	/**
	 * Order of a 4x4 matrix.
	 */
	public static final int ORDER = 4;

	/**
	 * 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = new Matrix.Builder().identity().build();

	/**
	 * Layout of a 4x4 matrix.
	 */
	public static final Component LAYOUT = Component.floats(ORDER * ORDER);

	/**
	 * Length of a 4x4 matrix (bytes)
	 */
	public static final int LENGTH = LAYOUT.stride();

	/**
	 * Creates a 4x4 translation matrix by populating the top-right column of the matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Tuple vec) {
		return new Builder()
				.identity()
				.column(3, vec)
				.build();
	}

	/**
	 * Creates a 4x4 scaling matrix by populating the diagonal of the matrix.
	 * @return Scaling matrix
	 */
	public static Matrix scale(float x, float y, float z) {
		return new Builder()
				.set(0, 0, x)
				.set(1, 1, y)
				.set(2, 2, z)
				.set(3, 3, 1)
				.build();
	}

	/**
	 * Constructor.
	 */
	Matrix4() {
		super(ORDER);
	}

	@Override
	public int order() {
		return ORDER;
	}

	@Override
	protected int index(int row, int col) {
		return row + (col << 2);
	}

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public String toString() {
		if(this == IDENTITY) {
			return "IDENTITY";
		}
		else {
			return super.toString();
		}
	}
}
