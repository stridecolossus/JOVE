package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Counter-clockwise rotation angle (radians)
	 */
	float angle();

	/**
	 * Creates a matrix for the given rotation.
	 * @param rot Rotation
	 * @return New rotation matrix
	 * @throws UnsupportedOperationException for an <i>arbitrary</i> axis
	 * @see Quaternion#of(Rotation)
	 */
	static Matrix matrix(Vector axis, float angle) {
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
	 * Skeleton implementation.
	 */
	abstract class AbstractRotation implements Rotation {
		private final Vector axis;
		protected float angle;

		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Angle
		 */
		protected AbstractRotation(Vector axis, float angle) {
			this.axis = notNull(axis);
			this.angle = angle;
		}

		@Override
		public final Vector axis() {
			return axis;
		}

		@Override
		public final float angle() {
			return angle;
		}

		@Override
		public int hashCode() {
			return Objects.hash(axis, angle);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Rotation that) &&
					this.axis.equals(that.axis()) &&
					MathsUtil.isEqual(this.angle, that.angle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(axis).append(angle).build();
		}
	}

	/**
	 * Default implementation.
	 */
	class DefaultRotation extends AbstractRotation {
		private final Matrix matrix;

		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Angle
		 */
		public DefaultRotation(Vector axis, float angle) {
			super(axis, angle);
			this.matrix = Rotation.matrix(axis, angle);
		}

		@Override
		public Matrix matrix() {
			return matrix;
		}
	}
}
