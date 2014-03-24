package org.sarge.jove.scene;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MutableMatrix;
import org.sarge.jove.geometry.MutablePoint;
import org.sarge.jove.geometry.MutableVector;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Camera position and view.
 * @author Sarge
 */
public class Camera {
	// Camera model
	private final MutablePoint pos = new MutablePoint( 0, 0, 5 );
	private final MutablePoint target = new MutablePoint( Point.ORIGIN );
	private final MutableVector dir = new MutableVector( Vector.Z_AXIS.invert() );
	private boolean dirty;

	// Axes
	private Vector up = Vector.Y_AXIS;
	private MutableVector right = new MutableVector();

	// Working data
	private final MutableMatrix matrix = new MutableMatrix( 4 );
	private final MutableVector y = new MutableVector();
	private final MutableVector z  = new MutableVector();
	private final MutableVector trans = new MutableVector();

	/**
	 * Constructor.
	 */
	public Camera() {
		update();
	}

	/**
	 * @return Camera location
	 */
	public Point getPosition() {
		return pos;
	}

	/**
	 * Sets the location of the camera.
	 * @param pos
	 */
	public void setPosition( Point pos ) {
		Check.notNull( pos );
		this.pos.set( pos );
		dir.subtract( pos, target ).normalize();
		dirty = true;
	}

	/**
	 * @return Camera target position
	 */
	public Point getTarget() {
		return target;
	}

	/**
	 * Sets the camera target.
	 * @param target Camera target
	 */
	public void setTarget( Point target ) {
		Check.notNull( target );
		this.target.set( target );
		dir.subtract( pos, target ).normalize();
		dirty = true;
	}

	/**
	 * @return Camera view direction
	 */
	public Vector getDirection() {
		return dir;
	}

	/**
	 * @return Up axis
	 */
	public Vector getUpDirection() {
		return up;
	}

	/**
	 * Sets the camera up direction.
	 * @param up
	 */
	public void setUpDirection( Vector up ) {
		Check.notNull( up );
		this.up = up.normalize();
		dirty = true;
	}

	/**
	 * @return Right axis of this camera
	 */
	public Vector getRightAxis() {
		return right;
	}

	/**
	 * @return View matrix for this camera
	 */
	public Matrix getViewMatrix() {
		if( dirty ) {
			update();
		}

		return matrix;
	}

	/**
	 * Moves the camera position in the current direction.
	 * @param amount Movement amount
	 */
	public void move( float amount ) {
		move( dir.multiply( amount ) );
	}

	/**
	 * Strafes the camera position (positive is to the right).
	 * @param amount
	 * @see #getRightAxis()
	 */
	public void strafe( float amount ) {
		move( right.multiply( amount ) );
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vec Movement vector
	 */
	public void move( Vector vec ) {
		pos.add( vec );
		target.add( vec );
		dirty = true;
	}

	/**
	 * Rotates this camera counter-clockwise about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public void rotate( Quaternion rot ) {
		rot.rotate( dir );
		// TODO - calc new target position
		dirty = true;
	}

	/**
	 * Orbits the camera by the given rotation about the target position.
	 * @param rot Rotation
	 */
	public void orbit( Quaternion rot ) {
		trans.subtract( target, pos );
		pos.set( target );
		rot.rotate( trans );

		pos.add( trans );
		dir.subtract( pos, target ).normalize();

		dirty = true;
	}

	/**
	 * Updates the camera axes and view matrix after a change in position or orientation.
	 */
	private void update() {
		// Derive camera axes
		z.set( dir ).invert();
		right.set( up ).cross( z ).normalize();
		y.set( z ).cross( right ).normalize();

		// Set camera axes
		matrix.setRow( 0, right );
		matrix.setRow( 1, y );
		matrix.setRow( 2, z );

		// Set camera translation
		trans.set( right.dot( pos ), y.dot( pos ), z.dot( pos ) );
		matrix.setColumn( 3, trans.invert() );

		// Note camera is updated
		dirty = false;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
