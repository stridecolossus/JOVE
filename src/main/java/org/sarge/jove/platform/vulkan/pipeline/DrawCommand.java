package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.core.Command;

/**
 * A <i>draw command</i> is used to draw a model.
 * @author Sarge
 */
public interface DrawCommand extends Command {
	/**
	 * Creates a drawing command.
	 * @param model Model to draw
	 * @return Drawing command
	 */
	static DrawCommand of(Model model) {
		if(model.index().isPresent()) {
			return (api, handle) -> api.vkCmdDrawIndexed(handle, model.count(), 1, 0, 0, 0);
		}
		else {
			return (api, handle) -> api.vkCmdDraw(handle, model.count(), 1, 0, 0);
		}
	}
	// TODO - instancing, offset, etc
}
