package org.sarge.jove.common;

import java.nio.FloatBuffer;

/**
 * Defines a floating-point data-type that can be appended to a VBO buffer.
 * @author Sarge
 */
public interface Appendable {
	/**
	 * Appends this data to the given buffer.
	 * @param buffer Vertex buffer
	 */
	void append( FloatBuffer buffer );
}
