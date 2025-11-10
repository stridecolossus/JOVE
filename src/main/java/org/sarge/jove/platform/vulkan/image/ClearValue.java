package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.Percentile;

/**
 * A <i>clear value</i> specifies the clearing operation for an attachment before the render pass begins.
 * @author Sarge
 */
public sealed interface ClearValue {
	/**
	 * @return Expected image aspect
	 */
	VkImageAspect aspect();

	/**
	 * Populates the given clear value descriptor.
	 * @param value Descriptor
	 */
	void populate(VkClearValue descriptor);

	/**
	 * Clear value for a colour attachment.
	 */
	record ColourClearValue(Colour colour) implements ClearValue {
		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.COLOR;
		}

		@Override
		public void populate(VkClearValue clear) {
			clear.color = new VkClearColorValue();
			clear.color.float32 = colour.toArray();
		}
	}

	/**
	 * Clear value for the depth-stencil attachment.
	 */
	record DepthClearValue(Percentile depth) implements ClearValue {
		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.DEPTH;
		}

		@Override
		public void populate(VkClearValue clear) {
			clear.depthStencil = new VkClearDepthStencilValue();
			clear.depthStencil.depth = depth.value();
			clear.depthStencil.stencil = 0;
		}
	}
}
