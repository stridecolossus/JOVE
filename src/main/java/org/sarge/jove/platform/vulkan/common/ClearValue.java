package org.sarge.jove.platform.vulkan.common;

import java.util.Set;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.util.Check;

/**
 * A <i>clear value</i> populates the clear descriptor for an attachment.
 */
public interface ClearValue {
	/**
	 * Populates a given clear value descriptor.
	 * @param value Descriptor
	 */
	void populate(VkClearValue value);

	/**
	 * @return Whether the given image aspect is valid for this clear value
	 */
	boolean isValid(VkImageAspectFlag aspect);

	/**
	 * Default clear colour.
	 */
	ClearValue COLOUR = of(Colour.BLACK);

	/**
	 * Default depth clear value.
	 */
	ClearValue DEPTH = depth(1);

	/**
	 * Empty clear value.
	 */
	ClearValue NONE = new ClearValue() {
		@Override
		public void populate(VkClearValue value) {
			// Does nowt
		}

		@Override
		public boolean isValid(VkImageAspectFlag aspect) {
			return switch(aspect) {
				case VK_IMAGE_ASPECT_COLOR_BIT -> true;
				case VK_IMAGE_ASPECT_DEPTH_BIT -> true;
				default -> false;
			};
		}

		@Override
		public String toString() {
			return "none";
		}
	};

	/**
	 * Creates a clear value for a colour attachment.
	 * @param col Colour
	 * @return New colour attachment clear value
	 */
	static ClearValue of(Colour col) {
		Check.notNull(col);

		return new ClearValue() {
			@Override
			public void populate(VkClearValue value) {
				value.setType("color");
				value.color.setType("float32");
				value.color.float32 = col.toArray();
			}

			@Override
			public boolean isValid(VkImageAspectFlag aspect) {
				return aspect == VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT;
			}

			@Override
			public boolean equals(Object obj) {
				// TODO
				return super.equals(obj);
			}

			@Override
			public String toString() {
				return String.format("colour(%s)", col);
			}
		};
	}

	/**
	 * Creates a clear value for a depth buffer attachment.
	 * @param depth Depth value 0..1
	 * @return New depth attachment clear value
	 * @throws IllegalArgumentException if the depth is not a valid 0..1 value
	 */
	static ClearValue depth(float depth) {
		Check.isPercentile(depth);

		return new ClearValue() {
			@Override
			public void populate(VkClearValue value) {
				value.setType("depthStencil");
				value.depthStencil.depth = depth;
				value.depthStencil.stencil = 0;
			}

			@Override
			public boolean isValid(VkImageAspectFlag aspect) {
				return aspect == VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT;
			}

			@Override
			public String toString() {
				return String.format("depth(%d)", depth);
			}
		};
	}

	/**
	 * Creates the default clear value for the given image aspects.
	 * @param aspects Image aspects
	 * @return Default clear value or {@code null} if not applicable
	 */
	static ClearValue of(Set<VkImageAspectFlag> aspects) {
		if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)) {
			return COLOUR;
		}
		else
		if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)) {
			return DEPTH;
		}
		else {
			return NONE;
		}
	}
}
