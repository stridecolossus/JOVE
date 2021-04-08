package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Percentile;

/**
 * A <i>clear value</i> specifies the clear operation for an attachment.
 */
public interface ClearValue {
	/**
	 * Populates the given clear value descriptor.
	 * @param value Descriptor
	 */
	void populate(VkClearValue value);

	/**
	 * @return Expected image aspect for this clear value
	 */
	VkImageAspectFlag aspect();

	/**
	 * Default clear colour.
	 */
	ClearValue COLOUR = new ColourClearValue(Colour.BLACK);

	/**
	 * Default clear value for a depth attachment.
	 */
	ClearValue DEPTH = new DepthClearValue(Percentile.ONE);

	/**
	 * Empty clear value.
	 */
	ClearValue NONE = new ClearValue() {
		@Override
		public VkImageAspectFlag aspect() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void populate(VkClearValue value) {
			// Does nowt
		}

		@Override
		public String toString() {
			return "none";
		}
	};

	/**
	 * Clear value for a colour attachment.
	 */
	class ColourClearValue implements ClearValue {
		private final Colour col;

		/**
		 * Constructor.
		 * @param col Clear colour
		 */
		public ColourClearValue(Colour col) {
			this.col = notNull(col);
		}

		@Override
		public VkImageAspectFlag aspect() {
			return VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT;
		}

		@Override
		public void populate(VkClearValue value) {
			value.setType("color");
			value.color.setType("float32");
			value.color.float32 = col.toArray();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof ColourClearValue that) && this.col.equals(that.col);
		}

		@Override
		public String toString() {
			return String.format("colour(%s)", col);
		}
	}

	/**
	 * Clear value for a depth attachment.
	 */
	class DepthClearValue implements ClearValue {
		private final float depth;

		public DepthClearValue(Percentile depth) {
			this.depth = depth.floatValue();
		}

		@Override
		public VkImageAspectFlag aspect() {
			return VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT;
		}

		@Override
		public void populate(VkClearValue value) {
			value.setType("depthStencil");
			value.depthStencil.depth = depth;
			value.depthStencil.stencil = 0;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof DepthClearValue that) && MathsUtil.isEqual(this.depth, that.depth);
		}

		@Override
		public String toString() {
			return String.format("depth(%d)", depth);
		}
	}
}
