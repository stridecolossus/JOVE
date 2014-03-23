package org.sarge.jove.model;

import org.sarge.jove.common.Bufferable;

/**
 * VBO component type.
 */
public interface BufferDataType {
	/**
	 * Size of a floating-point value in bytes.
	 */
	int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

	/**
	 * @return Size of this data-type (in bytes)
	 */
	int getSize();

	/**
	 * Looks up the buffer data for the given vertex.
	 * @param vertex Vertex data
	 * @return Buffer data
	 */
	Bufferable getData( Vertex vertex );
}
