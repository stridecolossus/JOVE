package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>rotation</i> defines a counter-clockwise rotation about an axis.
 * @author Sarge
 */
public interface Rotation {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Counter-clockwise rotation angle (radians)
	 */
	float angle();

	/**
	 * Default implementation.
	 */
	record DefaultRotation(Vector axis, float angle) implements Rotation {
		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Angle
		 */
		public DefaultRotation {
			Check.notNull(axis);
		}
	}

	/**
	 * Creates a matrix for the given rotation.
	 * @param rot Rotation
	 * @return New rotation matrix
	 * @throws UnsupportedOperationException if the axis is not pre-defined
	 * @see Quaternion#of(Rotation)
	 */
	static Matrix matrix(Rotation rot) {
		final Builder matrix = new Matrix.Builder().identity();
		final Vector axis = rot.axis();
		final float angle = rot.angle();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		if(Vector.X == axis) {
			matrix.set(1, 1, cos);
			matrix.set(1, 2, -sin);
			matrix.set(2, 1, sin);
			matrix.set(2, 2, cos);
		}
		else
		if(Vector.Y == axis) {
			matrix.set(0, 0, cos);
			matrix.set(0, 2, sin);
			matrix.set(2, 0, -sin);
			matrix.set(2, 2, cos);
		}
		else
		if(Vector.Z == axis) {
			matrix.set(0, 0, cos);
			matrix.set(0, 1, -sin);
			matrix.set(1, 0, sin);
			matrix.set(1, 1, cos);
		}
		else {
			throw new UnsupportedOperationException("Arbitrary axis not supported (use quaternion)");
		}
		return matrix.build();
	}
}
