package org.sarge.jove.common;

/**
 * A <i>transient native object</i> is defines a native resource that is managed by the application.
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
