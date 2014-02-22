package org.sarge.jove.model;

/**
 * Buffer access modes.
 */
public enum AccessMode {
	/**
	 * Fixed vertex data.
	 */
	STATIC,

	/**
	 * Dynamic mesh updated programatically.
	 */
	DYNAMIC,

	/**
	 * Streamed vertex data updated per frame.
	 */
	STREAM;

	/**
	 * @return Whether this access mode is dynamic
	 */
	public boolean isDynamic() {
		switch( this ) {
		case STATIC:
			return false;

		default:
			return true;
		}
	}
}

