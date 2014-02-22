package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Rotation specified as an axis-angle.
 * @author Sarge
 */
public class Rotation implements Transform {
	private final Vector axis;

	private Matrix matrix;
	private float angle;

	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public Rotation( Vector axis, float angle ) {
		Check.notNull( axis );
		this.axis = axis.normalize();
		setAngle( angle );
	}

	/**
	 * Constructor for zero rotation.
	 * @param axis Rotation axis
	 */
	public Rotation( Vector axis ) {
		this( axis, 0 );
	}

	/**
	 * @return Rotation axis
	 */
	public Vector getAxis() {
		return axis;
	}

	/**
	 * @return Rotation angle (radians)
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Angle (radians)
	 */
	public void setAngle( float angle ) {
		this.angle = angle;
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Matrix toMatrix() {
		if( dirty ) {
			// TODO - use proper rot -> matrix
			final Quaternion q = new Quaternion( axis, angle );
			matrix = q.toMatrix();
			dirty = false;
		}

		return matrix;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof Rotation ) {
			final Rotation that = (Rotation) obj;
			if( !this.axis.equals( that.axis ) ) return false;
			if( !MathsUtil.isEqual( this.angle, that.angle ) ) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
