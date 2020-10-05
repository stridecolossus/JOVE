package org.sarge.jove.platform.vulkan.util;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.util.Check;

/**
 * Nested builder for an image sub-resource range.
 * @param <T> Parent builder type
 * @author Sarge
 */
public class ImageResourceRangeBuilder<T> {
	private final T parent;
	private final Set<VkImageAspectFlag> aspects = new HashSet<>();
	private int baseMipLevel;
	private int levelCount = 1;
	private int baseArrayLayer;
	private int layerCount = 1;

	/**
	 * Constructor.
	 * @param parent Parent builder
	 */
	public ImageResourceRangeBuilder(T parent) {
		this.parent = notNull(parent);
	}

	/**
	 * Adds an image aspect to this range.
	 * @param aspect Image aspect
	 */
	public ImageResourceRangeBuilder<T> aspect(VkImageAspectFlag aspect) {
		Check.notNull(aspect);
		aspects.add(aspect);
		return this;
	}

	/**
	 * Sets the base mip level.
	 * @param baseMipLevel Base mip level
	 */
	public ImageResourceRangeBuilder<T> baseMipLevel(int baseMipLevel) {
		this.baseMipLevel = zeroOrMore(baseMipLevel);
		return this;
	}

	/**
	 * Sets the number of mip levels.
	 * @param levelCount Number of mip levels
	 */
	public ImageResourceRangeBuilder<T> levelCount(int levelCount) {
		this.levelCount = oneOrMore(levelCount);
		return this;
	}

	/**
	 * Sets the base array layer.
	 * @param baseArrayLayer Base array layer
	 */
	public ImageResourceRangeBuilder<T> baseArrayLayer(int baseArrayLayer) {
		this.baseArrayLayer = zeroOrMore(baseArrayLayer);
		return this;
	}

	/**
	 * Sets the number of array layers.
	 * @param layerCount Number of array layers
	 */
	public ImageResourceRangeBuilder<T> layerCount(int layerCount) {
		this.layerCount = oneOrMore(layerCount);
		return this;
	}

	/**
	 * @return Image sub-resource range descriptor
	 */
	public VkImageSubresourceRange result() {
		final VkImageSubresourceRange range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.mask(aspects);
		range.baseMipLevel = baseMipLevel;
		range.levelCount = levelCount;
		range.baseArrayLayer = baseArrayLayer;
		range.layerCount = layerCount;
		return range;
	}

	/**
	 * Constructs this image sub-resource range.
	 * @return Parent builder
	 */
	public T build() {
		return parent;
	}
}
