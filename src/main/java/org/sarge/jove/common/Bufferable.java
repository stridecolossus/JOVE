package org.sarge.jove.common;

import java.nio.FloatBuffer;

/**
 * Defines a floating-point data-type that can be appended to an NIO buffer.
 * @author Sarge
 * TODO
 * - should we be returning a Stream<Float>?  too much auto-boxing?
 * - component-size -> default?
 */
public interface Bufferable {
	/**
	 * @return Number of components of this data-type, e.g. 3 for a vector
	 */
	int getComponentSize();

	/**
	 * Appends this data to the given buffer.
	 * @param buffer Vertex buffer
	 */
	void append(FloatBuffer buffer);
}
