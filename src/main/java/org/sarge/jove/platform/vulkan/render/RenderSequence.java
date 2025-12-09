package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * A <i>render sequence</i> records the command sequence for a frame.
 * @author Sarge
 */
public interface RenderSequence {
	/**
	 * Records a command sequence to the given buffer.
	 * @param index		Frame index
	 * @param buffer	Command buffer to record
	 */
	void build(int index, Buffer buffer);
}
