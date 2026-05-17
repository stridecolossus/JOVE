package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;

/**
 * A <i>mutable rotation</i> specifies a rotation about a given axis.
 * @see AxisAngle
 * @author Sarge
 */
public class MutableRotation implements Transform {
	private AxisAngle rotation;

	/**
	 * Constructor.
	 * @param axis Axis of rotation
	 */
	public MutableRotation(Normal axis) {
		this.rotation = new AxisAngle(axis, 0);
	}

	/**
	 * Creates an adapter for this rotation using the given function to calculate the matrix.
	 * @param function Cosine function
	 * @return This rotation using the given function
	 */
	public MutableRotation with(CosineFunction function) {
		requireNonNull(function);

		return new MutableRotation(rotation.axis()) {
			@Override
			public Matrix matrix() {
				return rotation.matrix(function);
			}
		};
	}

	@Override
	public Matrix matrix() {
		return rotation.matrix(CosineFunction.DEFAULT);
	}

	/**
	 * Sets the angle of this rotation.
	 * @param angle Counter-clockwise rotation angle (radians)
	 */
	public void set(float angle) {
		set(new AxisAngle(rotation.axis(), angle));
	}

	/**
	 * Sets this rotation.
	 * @param rotation Rotation
	 */
	public void set(AxisAngle rotation) {
		this.rotation = requireNonNull(rotation);
	}
	// TODO - needed?

	@Override
	public int hashCode() {
		return rotation.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				obj == this ||
				obj instanceof MutableRotation that &&
				this.rotation.equals(that.rotation);
	}

	@Override
	public String toString() {
		return String.format("MutableRotation[%s]", rotation);
	}
}
