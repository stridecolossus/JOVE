package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return This rotation as an axis-angle
	 */
	AxisAngle rotation();

	/**
	 * An <i>axis-angle</i> is a simple fixed rotation about an axis.
	 */
	record AxisAngle(Vector axis, float angle) implements Rotation {
		@Override
		public AxisAngle rotation() {
			return this;
		}

		@Override
		public Matrix matrix() {
			final Builder matrix = new Matrix.Builder().identity();
			final float sin = MathsUtil.sin(angle);
			final float cos = MathsUtil.cos(angle);
			if(Vector.X.equals(axis)) {
				matrix.set(1, 1, cos);
				matrix.set(1, 2, -sin);
				matrix.set(2, 1, sin);
				matrix.set(2, 2, cos);
			}
			else
			if(Vector.Y.equals(axis)) {
				matrix.set(0, 0, cos);
				matrix.set(0, 2, sin);
				matrix.set(2, 0, -sin);
				matrix.set(2, 2, cos);
			}
			else
			if(Vector.Z.equals(axis)) {
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
}
