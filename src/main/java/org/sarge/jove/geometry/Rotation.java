package org.sarge.jove.geometry;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.util.MathsUtil;

/**
 * Counter-clockwise rotation about an arbitrary axis.
 * @author Sarge
 */
public interface Rotation {
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
		return new Rotation() {
			@Override
			public Vector axis() {
				return axis;
			}

			@Override
			public float angle() {
				return angle;
			}
		};
	}

	/**
	 * Mutable implementation.
	 */
	class MutableRotation implements Rotation, Transform {
		private final Vector axis;
		private float angle;

		private transient boolean dirty;
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

		/**
		 * @return This rotation as an animation
		 */
		public Animation animation() {
			return animator -> {
				final float angle = animator.position() * MathsUtil.TWO_PI;
				angle(angle);
			};
		}
	}
}
