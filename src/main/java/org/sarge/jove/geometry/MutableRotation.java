package org.sarge.jove.geometry;

/**
 * Template implementation for a mutable rotation about a given axis.
 * @author Sarge
 */
public abstract class MutableRotation extends AbstractAxisAngle {
	/**
	 * Creates a mutable rotation with an implementation appropriate to the given axis.
	 * @param vec Rotation axis
	 * @return New mutable rotation
	 */
	public static MutableRotation of(Vector vec) {
		if(vec instanceof Axis axis) {
			return new MutableRotation(axis) {
				@Override
				public Matrix matrix() {
					return axis.rotation(this.angle());
				}
			};
		}
		else {
			return new MutableRotation(vec) {
				@Override
				public Matrix matrix() {
					return Quaternion.of(this).matrix();
				}
			};
		}
	}

	private float angle;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	protected MutableRotation(Vector axis) {
		super(axis);
	}

	@Override
	public float angle() {
		return angle;
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void set(float angle) {
		this.angle = angle;
	}

	@Override
	public boolean isMutable() {
		return true;
	}
}
