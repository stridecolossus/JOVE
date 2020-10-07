package org.sarge.jove.platform.vulkan.util;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.util.Check;

/**
 * Nested builder for an image sub-resource range <b>or</b> layers.
 * <p>
 * Notes:
 * <ul>
 * <li>{@link #range()} creates a {@link VkImageSubresourceRange} descriptor (the {@code baseMipLevel) field is populated by {@link #mipLevel(int)}</li>
 * <li>{@link #layers()} creates a {@link VkImageSubresourceLayers} descriptor</li>
 * </ul>
 * <p>
 * @param <T> Parent builder type
 * @author Sarge
 * TODO - validate against the image
 */
public class ImageSubResourceBuilder<T> {
	private final T parent;
	private final Set<VkImageAspectFlag> aspects = new HashSet<>();
	private int mipLevel;
	private int levelCount = 1;
	private int baseArrayLayer;
	private int layerCount = 1;

	/**
	 * Constructor.
	 * @param parent Parent builder
	 */
	public ImageSubResourceBuilder(T parent) {
		this.parent = notNull(parent);
	}

	/**
	 * @return Number of specified image aspects
	 */
	public int aspectCount() {
		return aspects.size();
	}

	/**
	 * Adds an image aspect to this range.
	 * @param aspect Image aspect
	 */
	public ImageSubResourceBuilder<T> aspect(VkImageAspectFlag aspect) {
		Check.notNull(aspect);
		aspects.add(aspect);
		return this;
	}

	/**
	 * Sets the mip level (or the {@code baseMipLevel} field for a {@link VkImageSubresourceRange}).
	 * @param mipLevel Mip level
	 */
	public ImageSubResourceBuilder<T> mipLevel(int mipLevel) {
		this.mipLevel = zeroOrMore(mipLevel);
		return this;
	}

	/**
	 * Sets the number of mip levels.
	 * @param levelCount Number of mip levels
	 */
	public ImageSubResourceBuilder<T> levelCount(int levelCount) {
		this.levelCount = oneOrMore(levelCount);
		return this;
	}

	/**
	 * Sets the base array layer.
	 * @param baseArrayLayer Base array layer
	 */
	public ImageSubResourceBuilder<T> baseArrayLayer(int baseArrayLayer) {
		this.baseArrayLayer = zeroOrMore(baseArrayLayer);
		return this;
	}

	/**
	 * Sets the number of array layers.
	 * @param layerCount Number of array layers
	 */
	public ImageSubResourceBuilder<T> layerCount(int layerCount) {
		this.layerCount = oneOrMore(layerCount);
		return this;
	}

	/**
	 * @return Image sub-resource range descriptor
	 */
	public VkImageSubresourceRange range() {
		final VkImageSubresourceRange range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.mask(aspects);
		range.baseMipLevel = mipLevel;
		range.levelCount = levelCount;
		range.baseArrayLayer = baseArrayLayer;
		range.layerCount = layerCount;
		return range;
	}

	/**
	 * @return Image sub-resource layers descriptor
	 */
	public VkImageSubresourceLayers layers() {
		final VkImageSubresourceLayers layers = new VkImageSubresourceLayers();
		layers.aspectMask = IntegerEnumeration.mask(aspects);
		layers.mipLevel = mipLevel;
		layers.baseArrayLayer = baseArrayLayer;
		layers.layerCount = layerCount;
		return layers;
	}

	/**
	 * Constructs this image sub-resource.
	 * @return Parent builder
	 */
	public T build() {
		return parent;
	}
}
