package org.sarge.jove.common;

/**
 * OpenGL resource.
 * @author Sarge
 */
public interface GraphicResource {
	/**
	 * @return OpenGL resource ID
	 */
	int getResourceID();

	/**
	 * Releases this resource.
	 */
	void release();
}
