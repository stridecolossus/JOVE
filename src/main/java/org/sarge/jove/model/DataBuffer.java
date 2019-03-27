package org.sarge.jove.model;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.Resource;

/**
 * A <i>data buffer</i> is used to upload buffered data to the hardware.
 * @author Sarge
 */
public interface DataBuffer extends Resource {
	/**
	 * Pushes data to the hardware.
	 * @param buffer Data buffer
	 */
	void push(ByteBuffer buffer);

	/**
	 * @return VBO
	 */
	VertexBuffer toVertexBuffer();

	/**
	 * @return Index buffer
	 */
	IndexBuffer toIndexBuffer();
}
