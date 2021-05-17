package org.sarge.jove.common;

/**
 * A <i>native object</i> is a resource created by the native layer referenced by a JNA pointer.
 * @author Sarge
 */
public interface NativeObject {
	/**
	 * @return Handle
	 */
	Handle handle();
}
