package org.sarge.jove.model;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.vulkan.Command;

/**
 * A <i>index buffer</i> (IBO) is used to upload index data to the hardware.
 * @author Sarge
 */
public interface IndexBuffer extends Resource {
	/**
	 * Pushes indices to the hardware.
	 * @param buffer Indices buffer
	 */
	void push(ByteBuffer buffer);

	/**
	 * @return Command to bind this index buffer
	 */
	Command bind();
}
