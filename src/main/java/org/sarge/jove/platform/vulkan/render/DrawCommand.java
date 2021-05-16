package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.common.Command;

/**
 * A <i>draw command</i> is used to render a model.
 * @see Model
 * @author Sarge
 */
public interface DrawCommand extends Command {
	/**
	 * Helper - Creates a drawing command for the given model.
	 * @param model Model to draw
	 * @return Drawing command
	 */
	static Command of(Model model) {
		final int count = model.header().count();
		if(model.isIndexed()) {
			return indexed(count);
		}
		else {
			return draw(count);
		}
	}

	/**
	 * Creates a new draw command.
	 * @param count Number of vertices
	 * @return New draw command
	 */
	static Command draw(int count) {
		return (api, handle) -> api.vkCmdDraw(handle, count, 1, 0, 0);
	}

	/**
	 * Creates a new indexed draw command.
	 * @param count Number of vertices
	 * @return New indexed draw command
	 */
	static Command indexed(int count) {
		return (api, handle) -> api.vkCmdDrawIndexed(handle, count, 1, 0, 0, 0);
	}

	// TODO - instancing, offset, etc
}
