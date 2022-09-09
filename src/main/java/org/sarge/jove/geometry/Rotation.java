package org.sarge.jove.geometry;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return This rotation as an axis-angle
	 */
	AxisAngle toAxisAngle();

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
	abstract class AxisAngle implements Rotation {
		/**
		 * Creates a fixed axis-angle rotation.
		 * @param axis		Rotation axis
		 * @param angle		Angle (radians)
		 * @return New axis-angle
		 */
		public static AxisAngle of(Vector axis, float angle) {
			return new AxisAngle(axis, angle) {
				private final Matrix matrix = super.matrix();

				@Override
				public Matrix matrix() {
					return matrix;
				}
			};
		}

		protected final Vector axis;
		protected float angle;

		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Angle (radians)
		 */
		protected AxisAngle(Vector axis, float angle) {
			this.axis = axis.normalize();
			this.angle = angle;
		}

		/**
		 * @return Rotation axis
		 */
		public Vector axis() {
			return axis;
		}

		/**
		 * @return Rotation angle (radians)
		 */
		public float angle() {
			return angle;
		}

		@Override
		public AxisAngle toAxisAngle() {
			return this;
		}

		@Override
		public Matrix matrix() {
			if(axis instanceof Axis cardinal) {
				return cardinal.rotation(angle);
			}
			else {
				final AxisAngle rot = toAxisAngle();
				return Quaternion.of(rot).matrix();
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

		@Override
		public int hashCode() {
			return Objects.hash(axis, angle);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof AxisAngle that) &&
					this.axis.equals(that.axis()) &&
					MathsUtil.isEqual(this.angle, that.angle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(axis).append(angle).build();
		}
	}
}
