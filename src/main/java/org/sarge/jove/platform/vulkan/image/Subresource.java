package org.sarge.jove.platform.vulkan.image;
import static org.sarge.jove.util.Validation.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * An <i>image sub-resource</i> defines a subset of the aspects, mip levels and array layers of an image.
 * @author Sarge
 */
public interface Subresource {
	/**
	 * Special case identifier indicating the <i>remaining</i> number of mip levels or array layers.
	 */
	int REMAINING = (~0);

	/**
	 * @return Sub-resource aspects
	 */
	Set<VkImageAspect> aspects();

	/**
	 * @return Base MIP level
	 */
	int mipLevel();

	/**
	 * @return Number of MIP levels
	 */
	int levelCount();

	/**
	 * @return Base array layer
	 */
	int baseArrayLayer();

	/**
	 * @return Number of array layers
	 */
	int layerCount();

	/**
	 * Default implementation.
	 */
	record DefaultSubResource(Set<VkImageAspect> aspects, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) implements Subresource {
		/**
		 * Constructor.
		 * @param aspects				Sub resource aspects
		 * @param mipLevel				Base MIP level
		 * @param levelCount			Number of MIP levels
		 * @param baseArrayLayer		Base array layer
		 * @param layerCount			Number of array layers
		 */
		public DefaultSubResource {
			aspects = Set.copyOf(aspects);
			requireZeroOrMore(mipLevel);
			requireOneOrMore(levelCount);
			requireZeroOrMore(baseArrayLayer);
			requireOneOrMore(layerCount);
		}
	}

	/**
	 * Converts the given subresource to a Vulkan range descriptor.
	 * @param subresource Subresource
	 * @return Vulkan subresource range
	 */
	static VkImageSubresourceRange range(Subresource subresource) {
		final var range = new VkImageSubresourceRange();
		range.aspectMask = new EnumMask<>(subresource.aspects());
		range.baseMipLevel = subresource.mipLevel();
		range.levelCount = subresource.levelCount();
		range.baseArrayLayer = subresource.baseArrayLayer();
		range.layerCount = subresource.layerCount();
		return range;
	}

	/**
	 * Converts the given subresource to a Vulkan layers descriptor.
	 * @param subresource Subresource
	 * @return Vulkan subresource layers
	 */
	static VkImageSubresourceLayers layers(Subresource subresource) {
		final var layers = new VkImageSubresourceLayers();
		layers.aspectMask = new EnumMask<>(subresource.aspects());
		layers.mipLevel = subresource.mipLevel();
		layers.baseArrayLayer = subresource.baseArrayLayer();
		layers.layerCount = subresource.layerCount();
		return layers;
	}

	/**
	 * Builder for an image subresource.
	 */
	class Builder {
		private final Set<VkImageAspect> aspects = new HashSet<>();
		private int mipLevel;
		private int levelCount = 1;
		private int baseArrayLayer;
		private int layerCount = 1;

		/**
		 * Adds an image aspect.
		 * @param aspect Image aspect
		 */
		public Builder aspect(VkImageAspect aspect) {
			aspects.add(aspect);
			return this;
		}

		/**
		 * Sets the starting mip level.
		 * @param mipLevel Starting mip level
		 */
		public Builder mipLevel(int mipLevel) {
			this.mipLevel = mipLevel;
			return this;
		}

		/**
		 * Sets the number of mip levels.
		 * @param levelCount Number of mip levels
		 */
		public Builder levelCount(int levelCount) {
			this.levelCount = levelCount;
			return this;
		}

		/**
		 * Sets the starting array layer.
		 * @param baseArrayLayer Starting array layer
		 */
		public Builder baseArrayLayer(int baseArrayLayer) {
			this.baseArrayLayer = baseArrayLayer;
			return this;
		}

		/**
		 * Sets the number of array layers.
		 * @param layerCount Number of array layers
		 */
		public Builder layerCount(int layerCount) {
			this.layerCount = layerCount;
			return this;
		}

		/**
		 * Copies the properties of the given subresource.
		 * @param subresource Subresource to copy
		 */
		public Builder copy(Subresource subresource) {
			aspects.clear();
			aspects.addAll(subresource.aspects());
			mipLevel(subresource.mipLevel());
			levelCount(subresource.levelCount());
			baseArrayLayer(subresource.baseArrayLayer());
			layerCount(subresource.layerCount());
			return this;
		}

		/**
		 * Constructs this subresource.
		 * @return Subresource
		 */
		public Subresource build() {
			return new DefaultSubResource(aspects, mipLevel, levelCount, baseArrayLayer, layerCount);
		}
	}
}
