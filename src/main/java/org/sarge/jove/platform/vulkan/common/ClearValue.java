package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Percentile;

/**
 * A <i>clear value</i> specifies the clear operation for an attachment.
 * @author Sarge
 */
public sealed interface ClearValue {
	/**
	 * Populates the given clear value descriptor.
	 * @param value Descriptor
	 */
	void populate(VkClearValue value);

	/**
	 * @return Expected image aspect for this clear value
	 */
	VkImageAspect aspect();

	/**
	 * Default clear colour.
	 */
	ClearValue COLOUR = new ColourClearValue(Colour.BLACK);

	/**
	 * Default clear value for a depth attachment.
	 */
	ClearValue DEPTH = new DepthClearValue(Percentile.ONE);

	final class EmptyClearValue implements ClearValue {
		private EmptyClearValue() {
		}

		@Override
		public VkImageAspect aspect() {
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
	}

	/**
	 * Empty clear value.
	 */
	ClearValue NONE = new EmptyClearValue();
	/*
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
	*/

	/**
	 * Clear value for a colour attachment.
	 */
	final class ColourClearValue implements ClearValue {
		private final Colour col;

		/**
		 * Constructor.
		 * @param col Clear colour
		 */
		public ColourClearValue(Colour col) {
			this.col = notNull(col);
		}

		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.COLOR;
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
	final class DepthClearValue implements ClearValue {
		private final float depth;

		public DepthClearValue(Percentile depth) {
			this.depth = depth.floatValue();
		}

		@Override
		public VkImageAspect aspect() {
			return VkImageAspect.DEPTH;
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
