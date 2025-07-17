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
		public void populate(VkClearValue descriptor) {
			final var clear = new VkClearColorValue();
			clear.float32 = colour.toArray();
			descriptor.color = clear;
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
		public void populate(VkClearValue descriptor) {
			final var clear = new VkClearDepthStencilValue();
			clear.depth = depth.value();
			clear.stencil = 0;
			descriptor.depthStencil = clear;
		}
	}
}
