package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.scene.*;

public class VulkanRenderer {

	public void render(Renderable r) {
		switch(r) {
			case Material m -> {
				// bind pipeline
			}
			/**
			 * TODO - descriptor set
			 * bind
			 */
			case ModelNode n -> {
				// bind VBO
				// bind index
				// draw command
				// TODO - back to not needing the model but just the VBO/index buffer handles and the header?
			}
			default -> throw new UnsupportedOperationException();
		}
	}
}
