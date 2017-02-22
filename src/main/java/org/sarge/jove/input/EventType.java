package org.sarge.jove.input;

/**
 * Platform agnostic events.
 */
public enum EventType {
	/**
	 * Keyboard, mouse-button or screen-touch.
	 */
	PRESS,

	/**
	 * Keyboard, mouse-button or screen-touch release.
	 */
	RELEASE,

	/**
	 * Mouse double-click or screen double-tap.
	 */
	DOUBLE,

	/**
	 * Mouse or screen drag.
	 */
	DRAG,

	/**
	 * Mouse wheel zoom or screen pinch.
	 */
	ZOOM,

	/**
	 * Screen orientation/compass change.
	 */
	ORIENTATE;

	/**
	 * @return Whether this event type requires an event name
	 */
	public boolean hasName() {
		switch( this ) {
		case ZOOM:
		case ORIENTATE:
			return false;

		default:
			return true;
		}
	}

	/**
	 * @return Whether this event type requires a location argument
	 */
	public boolean hasLocation() {
		switch( this ) {
		case ORIENTATE:
		case ZOOM:
			return false;

		default:
			return true;
		}
	}
}
