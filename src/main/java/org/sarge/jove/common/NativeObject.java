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

	/**
	 * Helper - Extracts the handle from a potentially {@code null} native object.
	 * @param obj Native object
	 * @return Handle or {@code null} if the object is null
	 */
	static Handle ofNullable(NativeObject obj) {
		if(obj == null) {
			return null;
		}
		else {
			return obj.handle();
		}
	}
}
