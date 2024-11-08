package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.Percentile;

/**
 * A <i>clear value</i> specifies the clearing operation for an attachment before a render pass starts.
 * @author Sarge
 */
public interface ClearValue {
	/**
	 * @return Expected image aspect
	 */
	VkImageAspect aspect();

	/**
	 * Populates the given clear value descriptor.
	 * @param value Descriptor
	 */
	void populate(VkClearValue value);

	/**
	 * Clear value for a colour attachment.
	 */
	record ColourClearValue(Colour colour) implements ClearValue {
		/**
		 * Constructor.
		 * @param colour Clear colour
		 */
		public ColourClearValue {
			requireNonNull(colour);
		}

		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.COLOR;
		}

		@Override
		public void populate(VkClearValue value) {
			value.setType("color");
			value.color.setType("float32");
			value.color.float32 = colour.toArray();
		}
	}

	/**
	 * Clear value for a depth attachment.
	 */
	record DepthClearValue(Percentile depth) implements ClearValue {
		/**
		 * Default clear value for a depth attachment.
		 */
		public static final DepthClearValue DEFAULT = new DepthClearValue(Percentile.ONE);

		/**
		 * Constructor.
		 * @param depth Depth value
		 */
		public DepthClearValue {
			requireNonNull(depth);
		}

		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.DEPTH;
		}

		@Override
		public void populate(VkClearValue value) {
			value.setType("depthStencil");
			value.depthStencil.depth = depth.floatValue();
			value.depthStencil.stencil = 0;
		}
	}
}
