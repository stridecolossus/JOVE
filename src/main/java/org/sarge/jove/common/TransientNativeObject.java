package org.sarge.jove.common;

/**
 * A <i>transient native object</i> is a native resource managed by the application.
 * @author Sarge
 */
public interface TransientNativeObject extends NativeObject {
	/**
	 * Destroys this object.
	 * @throws IllegalStateException if this object has already been destroyed
	 */
	void destroy();

	/**
	 * @return Whether this object has been destroyed
	 */
	boolean isDestroyed();
}
