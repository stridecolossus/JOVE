package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;

/**
 * Utility class providing various convenience constants and factory methods for common transformations.
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
 * The top-left 3x3 sub-matrix is the view orientation where (in camera terms):
 * <ul>
 * <li>R is the <i>right</i> vector</li>
 * <li>Y is <i>up</i></li>
 * <li>and D is the view <i>direction</i> (in camera terms)</li>
 * </ul>
 * <p>
 * The right-hand column T is the view translation (or eye position).
 * <p>
 * Note that both components are inverted (transposed and negated) since the scene is transformed in the opposite direction to the view (or camera).
 * <p>
 * @author Sarge
 */
public final class Matrix4 {
	/**
	 * Order of a 4x4 matrix.
	 */
	public static final int ORDER = 4;

	/**
	 * 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = builder().identity().build();

	/**
	 * Layout of a 4x4 matrix.
	 */
	public static final Layout LAYOUT = Layout.floats(ORDER * ORDER);

	/**
	 * Length of a 4x4 matrix (bytes)
	 */
	public static final int LENGTH = LAYOUT.stride();

	private Matrix4() {
	}

	/**
	 * @return New builder for a 4x4 matrix
	 */
	public static Matrix.Builder builder() {
		return new Matrix.Builder(ORDER);
	}

	/**
	 * Creates a 4x4 translation matrix by populating the top-right column of the matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		return builder()
				.identity()
				.column(3, vec)
				.build();
	}

	/**
	 * Creates a 4x4 scaling matrix by populating the diagonal of the matrix.
	 * @return Scaling matrix
	 */
	public static Matrix scale(float x, float y, float z) {
		return builder()
				.set(0, 0, x)
				.set(1, 1, y)
				.set(2, 2, z)
				.set(3, 3, 1)
				.build();
	}
}
