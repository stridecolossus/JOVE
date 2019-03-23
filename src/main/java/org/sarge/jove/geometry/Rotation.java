package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Counter-clockwise rotation about an arbitrary axis.
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
	 * Creates a fixed rotation.
	 * @param axis		Axis
	 * @param angle		Rotation angle (radians)
	 * @return Rotation
	 */
	static Rotation of(Vector axis, float angle) {
		return new MutableRotation(axis, angle);
	}

	/**
	 * Mutable implementation.
	 */
	class MutableRotation implements Rotation {
		private final Vector axis;
		private float angle;
		private boolean dirty;
		private transient Matrix matrix;

		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Angle (radians)
		 */
		public MutableRotation(Vector axis, float angle) {
			this.axis = notNull(axis);
			angle(angle);
		}

		@Override
		public Vector axis() {
			return axis;
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
			this.matrix = Quaternion.of(this).matrix();
			this.dirty = true;
		}

		@Override
		public Matrix matrix() {
			dirty = false;
			return matrix;
		}

		@Override
		public boolean isDirty() {
			return dirty;
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
