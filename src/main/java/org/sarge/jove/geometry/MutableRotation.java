package org.sarge.jove.geometry;

import java.util.function.Supplier;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableRotation extends AbstractRotation {
	private final Supplier<Matrix> factory;
	private float angle;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		super(axis);
		this.factory = () -> Quaternion.of(this).matrix();
	}

	/**
	 * Constructor for a rotation about a cardinal axis.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Axis axis) {
		super(axis.vector());
		this.factory = () -> axis.rotation(angle);
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

	@Override
	public Matrix matrix() {
		return factory.get();
	}
}
