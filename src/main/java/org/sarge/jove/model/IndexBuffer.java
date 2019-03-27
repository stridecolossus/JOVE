package org.sarge.jove.model;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.vulkan.Command;

/**
 * A <i>vertex buffer</i> (VBO) is used to upload vertex data to the hardware.
 * @author Sarge
 */
public interface IndexBuffer extends Resource {
	/**
	 * Pushes data to the hardware.
	 * @param buffer Data buffer
	 */
	void push(ByteBuffer buffer);

	/**
	 * @return Command to bind this buffer
	 */
	Command bind();
}
