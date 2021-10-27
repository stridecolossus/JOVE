package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> is a counter-clockwise rotation about an arbitrary axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Rotation angle (radians)
	 */
	float angle();

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractRotation implements Rotation {
		private final Vector axis;

		/**
		 * Constructor.
		 * @param axis Rotation axis
		 */
		protected AbstractRotation(Vector axis) {
			this.axis = notNull(axis);
		}

		@Override
		public final Vector axis() {
			return axis;
		}

		@Override
		public Matrix matrix() {
			return Quaternion.of(this).matrix();
		}

		@Override
		public final boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Rotation that) &&
					this.axis.equals(that.axis()) &&
					MathsUtil.isEqual(this.angle(), that.angle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(axis)
					.append(angle())
					.build();
		}
	}

	/**
	 * Creates a fixed rotation.
	 * @param axis		Axis
	 * @param angle		Rotation angle (radians)
	 * @return Fixed rotation
	 */
	static Rotation of(Vector axis, float angle) {
		return new AbstractRotation(axis) {
			@Override
			public float angle() {
				return angle;
			}
		};
	}

	/**
	 * Mutable implementation.
	 */
	class MutableRotation extends AbstractRotation {
		private float angle;
		private boolean dirty = true;

		/**
		 * Constructor.
		 * @param axis Rotation axis
		 */
		public MutableRotation(Vector axis) {
			super(axis);
		}

		@Override
		public float angle() {
			return angle;
		}

		/**
		 * Sets the rotation angle.
		 * @param angle Angle (radians)
		 */
		public void angle(float angle) {
			this.angle = angle;
			dirty = true;
		}

		@Override
		public Matrix matrix() {
			dirty = false;
			return super.matrix();
		}

		@Override
		public boolean isDirty() {
			return dirty;
		}
	}
}
