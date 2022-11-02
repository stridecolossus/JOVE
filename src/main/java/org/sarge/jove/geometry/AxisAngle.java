package org.sarge.jove.geometry;

/**
 * An <i>axis-angle</i> defines a counter-clockwise rotation about an axis.
 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
 */
public interface AxisAngle extends Rotation {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Rotation angle (radians)
	 */
	float angle();

	/**
	 * Creates a fixed axis-angle rotation.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return New axis-angle
	 */
	static AxisAngle of(Vector axis, float angle) {
		return new AbstractAxisAngle(axis) {
			private final Matrix matrix = Quaternion.of(this).matrix();

			@Override
			public float angle() {
				return angle;
			}

			@Override
			public Matrix matrix() {
				return matrix;
			}
		};
	}
}
