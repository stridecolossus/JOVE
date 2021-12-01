package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.lib.util.Check;
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
		public boolean equals(Object obj) {
			return obj == this;
		}

		@Override
		public String toString() {
			return "None";
		}
	};

	/**
	 * Clear value for a colour attachment.
	 */
	record ColourClearValue(Colour col) implements ClearValue {
		/**
		 * Constructor.
		 * @param col Clear colour
		 */
		public ColourClearValue {
			Check.notNull(col);
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
		public DepthClearValue(Percentile depth) {
			this.depth = notNull(depth);
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
