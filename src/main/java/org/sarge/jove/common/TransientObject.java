package org.sarge.jove.common;

/**
 * A <i>transient object</i> is a resource managed by the application.
 * @author Sarge
 */
public interface TransientObject {
	/**
	 * Destroys this object.
	 * @throws IllegalStateException if this object has already been destroyed
	 */
	void destroy();

	/**
	 * @return Whether this object has been destroyed
	 */
	default boolean isDestroyed() {
		return false;
	}
}
