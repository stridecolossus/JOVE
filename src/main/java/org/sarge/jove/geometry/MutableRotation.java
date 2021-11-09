package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Rotation.AbstractRotation;

/**
 * Mutable implementation.
 */
public class MutableRotation extends AbstractRotation {
	/**
	 * Helper - Defines the rotation factory method.
	 */
	@FunctionalInterface
	private static interface Factory {
		Transform matrix(Vector axis, float angle);
	}

	private final MutableRotation.Factory factory;
	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		super(axis, 0);
		this.factory = isAxis(axis) ? Rotation::matrix : Quaternion::of;
	}

	/**
	 * @return Whether the given vector is a pre-defined or arbitrary axis
	 */
	private static boolean isAxis(Vector vec) {
		return (vec == Vector.X) || (vec == Vector.Y) || (vec == Vector.Z);
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void angle(float angle) {
		this.angle = angle;
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Matrix matrix() {
		dirty = false;
		return factory.matrix(axis, angle).matrix();
	}
}