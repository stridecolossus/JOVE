package org.sarge.jove.geometry;

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
	 * Rotates the given vector by this rotation.
	 * @param vec Vector
	 * @return Rotated vector
	 */
	Vector rotate(Vector vec);

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
			if(axis instanceof Axis cardinal) {
				return cardinal.rotation(angle);
			}
			else {
				return Quaternion.of(axis, angle).matrix();
			}
		}

		/**
		 * Rotates the given vector by this axis-angle.
		 * <p>
		 * This method is implemented using <i>Rodrigues' rotation formula</i> expressed as:
		 * <pre>rot = cos(a)v + sin(a)(n x v) + (1 - cos(a))(n.v)n</pre>
		 * Where:
		 * <ul>
		 * <li><i>a</i> is the rotation angle</li>
		 * <li><i>n</i> is the axis (or normal)</li>
		 * <li>and <i>v</i> is the vector to be rotated</li>
		 * </ul>
		 * <p>
		 * This approach may be more efficient than constructing a rotation matrix or quaternion for certain use-cases.
		 */
		@Override
		public Vector rotate(Vector vec) {
			final float cos = MathsUtil.cos(angle);
			final Vector a = vec.multiply(cos);
			final Vector b = axis.cross(vec).multiply(MathsUtil.sin(angle));
			final Vector c = axis.multiply((1 - cos) * axis.dot(vec));
			return a.add(b).add(c);
		}
	}
}
