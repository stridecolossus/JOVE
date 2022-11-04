package org.sarge.jove.geometry;

/**
 * Template implementation for a mutable rotation about a given axis.
 * @author Sarge
 */
public abstract class MutableRotation extends AbstractAxisAngle {
	/**
	 * Creates a mutable rotation with an implementation appropriate to the given axis.
	 * @param axis Rotation axis
	 * @return New mutable rotation
	 */
	public static MutableRotation of(Normal axis) {
		if(axis instanceof Axis cardinal) {
			return new MutableRotation(cardinal) {
				@Override
				public Matrix matrix() {
					return cardinal.rotation(this.angle());
				}
			};
		}
		else {
			return new MutableRotation(axis) {
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
	protected MutableRotation(Normal axis) {
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
