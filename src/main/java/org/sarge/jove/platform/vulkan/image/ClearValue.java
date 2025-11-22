package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.Percentile;

/**
 * A <i>clear value</i> specifies the clearing operation for an attachment before the render pass begins.
 * @author Sarge
 */
public sealed interface ClearValue {
	/**
	 * Unused clear value.
	 */
	record None() implements ClearValue {
	}

	/**
	 * Clear value for a colour attachment.
	 */
	record ColourClearValue(Colour colour) implements ClearValue {
	}

	/**
	 * Clear value for the depth-stencil attachment.
	 */
	record DepthClearValue(Percentile depth) implements ClearValue {
	}

	/**
	 * Builds the clear value descriptor for a framebuffer attachment.
	 * @param clear Clear value
	 * @return Clear value descriptor
	 */
	static VkClearValue populate(ClearValue clear) {
		final var descriptor = new VkClearValue();

		switch(clear) {
    		case None _ -> {
    			// Empty
    		}

			case ColourClearValue(Colour colour) -> {
				descriptor.color = new VkClearColorValue();
				descriptor.color.float32 = colour.toArray();
			}

			case DepthClearValue(Percentile depth) -> {
				descriptor.depthStencil = new VkClearDepthStencilValue();
				descriptor.depthStencil.depth = depth.value();
				descriptor.depthStencil.stencil = 0;
			}
		}

		return descriptor;
	}
}
