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
	 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
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

		/**
		 * Rotates the given vector by this axis-angle using Rodrigues' Rotation Formula.
		 * @param vec Vector to rotate
		 * @return Rotated vector
		 */
		public Vector rotate(Vector vec) {
			final float cos = MathsUtil.cos(angle);
			final Vector a = vec.multiply(cos);
			final Vector b = axis.cross(vec).multiply(MathsUtil.sin(angle));
			final Vector c = axis.multiply((1 - cos) * axis.dot(vec));
			return a.add(b).add(c);
		}
	}
}
