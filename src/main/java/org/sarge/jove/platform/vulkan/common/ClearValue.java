package org.sarge.jove.platform.vulkan.common;

import static org.sarge.jove.util.Check.notNull;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkClearValue;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.util.Check;

/**
 * A <i>clear value</i> populates the clear descriptor for an attachment.
 */
public abstract class ClearValue {
	/**
	 * Default clear colour.
	 */
	public static final ClearValue COLOUR = of(Colour.BLACK);

	/**
	 * Default depth clear value.
	 */
	public static final ClearValue DEPTH = depth(1);

	/**
	 * Creates a clear value for a colour attachment.
	 * @param col Colour
	 * @return New colour attachment clear value
	 */
	public static ClearValue of(Colour col) {
		return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, col) {
			@Override
			public void populate(VkClearValue value) {
				value.setType("color");
				value.color.setType("float32");
				value.color.float32 = col.toArray();
			}
		};
	}

	/**
	 * Creates a clear value for a depth buffer attachment.
	 * @param depth Depth value 0..1
	 * @return New depth attachment clear value
	 * @throws IllegalArgumentException if the depth is not a valid 0..1 value
	 */
	public static ClearValue depth(float depth) {
		Check.isPercentile(depth);
		return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, depth) {
			@Override
			public void populate(VkClearValue value) {
				value.setType("depthStencil");
				value.depthStencil.depth = depth;
				value.depthStencil.stencil = 0;
			}
		};
	}

	/**
	 * Creates the default clear value for the given image aspects.
	 * @param aspects Image aspects
	 * @return Default clear value or {@code null} if not applicable
	 */
	public static ClearValue of(Set<VkImageAspectFlag> aspects) {
		if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)) {
			return COLOUR;
		}
		else
		if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)) {
			return DEPTH;
		}
		else {
			return null;
		}
	}

	private final VkImageAspectFlag aspect;
	private final Object arg;

	/**
	 * Constructor.
	 * @param aspect		Expected image aspect
	 * @param arg			Clear argument
	 */
	private ClearValue(VkImageAspectFlag aspect, Object arg) {
		this.aspect = notNull(aspect);
		this.arg = notNull(arg);
	}

	/**
	 * @return Expected image aspect
	 */
	public VkImageAspectFlag aspect() {
		return aspect;
	}

	/**
	 * @return Population function
	 */
	public abstract void populate(VkClearValue value);

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof ClearValue that) &&
				(this.aspect == that.aspect) &&
				this.arg.equals(that.arg);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("aspect", aspect)
				.append("arg", arg)
				.build();
	}
}
