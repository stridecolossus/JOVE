package org.sarge.jove.input;

import org.sarge.jove.geometry.MutablePoint;
import org.sarge.jove.geometry.MutableVector;
import org.sarge.jove.scene.Camera;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Camera zoom controller.
 * @author Sarge
 */
public class CameraZoomAction implements Action {
	private final Camera cam;

	private float sensitivity = 0.25f;

	/**
	 * Constructor.
	 * @param cam Camera
	 */
	public CameraZoomAction( Camera cam ) {
		Check.notNull( cam );
		this.cam = cam;
	}

	@Override
	public String getName() {
		return "camera-zoom";
	}

	/**
	 * @return Camera sensitivity
	 */
	public float getSensitivity() {
		return sensitivity;
	}

	/**
	 * Sets the rotation sensitivity.
	 * @param sensitivity
	 */
	public void setSensitivity( float sensitivity ) {
		this.sensitivity = sensitivity;
	}

	private final MutableVector dir = new MutableVector();
	private final MutablePoint pos = new MutablePoint();

	@Override
	public void execute( InputEvent event ) {
		// Verify event
		if( event.getEventKey().getType() != EventType.ZOOM ) throw new IllegalArgumentException( "Expected zoom event" );

		// Calc zoom vector
		dir.set( cam.getDirection() );
		dir.multiply( -event.getZoom() * sensitivity );

		// Add to camera
		pos.set( cam.getPosition() );
		pos.add( dir );
		cam.setPosition( pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
