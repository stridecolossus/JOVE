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
public interface ClearValue {
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
	 * Empty clear value for an attachment that is not cleared.
	 * @throws UnsupportedOperationException for all operations
	 */
	ClearValue NONE = new ClearValue() {
		@Override
		public VkImageAspect aspect() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void populate(VkClearValue value) {
			throw new UnsupportedOperationException();
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
			return (obj == this) || (obj instanceof ColourClearValue that) && this.col.equals(that.col);
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
		/**
		 * Default clear value for a depth attachment.
		 */
		public static final ClearValue DEFAULT = new DepthClearValue(Percentile.ONE);

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
			return (obj == this) || (obj instanceof DepthClearValue that) && MathsUtil.isEqual(this.depth, that.depth);
		}

		@Override
		public String toString() {
			return String.format("depth(%f)", depth);
		}
	}
}
