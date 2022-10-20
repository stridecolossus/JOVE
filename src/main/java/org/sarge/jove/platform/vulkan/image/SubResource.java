package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;

/**
 * An <i>image sub-resource</i> defines a subset of the aspects, mip levels and array layers of an image.
 * @author Sarge
 */
public interface SubResource {
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
	 * @return New sub-resource range descriptor
	 */
	default VkImageSubresourceRange toRange() {
		final var range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.reduce(aspects());
		range.baseMipLevel = mipLevel();
		range.levelCount = levelCount();
		range.baseArrayLayer = baseArrayLayer();
		range.layerCount = layerCount();
		return range;
	}

	/**
	 * @return New sub-resource layers descriptor
	 */
	default VkImageSubresourceLayers toLayers() {
		final var layers = new VkImageSubresourceLayers();
		layers.aspectMask = IntegerEnumeration.reduce(aspects());
		layers.mipLevel = mipLevel();
		layers.baseArrayLayer = baseArrayLayer();
		layers.layerCount = layerCount();
		return layers;
	}

	/**
	 * Builder for an image sub-resource.
	 */
	public static class Builder {
		private final Descriptor descriptor;
		private Set<VkImageAspect> aspects = new HashSet<>();
		private int mipLevel;
		private int levelCount = 1;
		private int baseArrayLayer;
		private int layerCount = 1;

		/**
		 * Constructor.
		 * @param parent Parent image descriptor
		 */
		public Builder(Descriptor descriptor) {
			this.descriptor = notNull(descriptor);
		}

		/**
		 * Adds an image aspect.
		 * @param aspect Image aspect
		 * @throws IllegalArgumentException if the given aspect is not a member of the parent
		 */
		public Builder aspect(VkImageAspect aspect) {
			if(!descriptor.aspects().contains(aspect)) {
				throw new IllegalArgumentException(String.format("Aspect must be member of the image: aspect=%s image=%s", aspect, descriptor.aspects()));
			}
			aspects.add(aspect);
			return this;
		}

		/**
		 * Sets the starting mip level.
		 * @param mipLevel Starting mip level
		 * @throws IllegalArgumentException if the mip level exceeds the parent
		 */
		public Builder mipLevel(int mipLevel) {
			if(mipLevel >= descriptor.levelCount()) {
				throw new IllegalArgumentException(String.format("Invalid mip level: %d/%d", mipLevel, descriptor.levelCount()));
			}
			this.mipLevel = zeroOrMore(mipLevel);
			return this;
		}

		/**
		 * Sets the number of mip levels.
		 * @param levelCount Number of mip levels
		 * @throws IllegalArgumentException if the number of levels is higher than the parent
		 */
		public Builder levelCount(int levelCount) {
			if(levelCount > descriptor.levelCount()) {
				throw new IllegalArgumentException(String.format("Invalid level count: count=%d parent=%d", levelCount, descriptor.levelCount()));
			}
			this.levelCount = oneOrMore(levelCount);
			return this;
		}

		/**
		 * Sets the starting array layer.
		 * @param baseArrayLayer Starting array layer
		 * @throws IllegalArgumentException if the base layer exceeds the parent
		 */
		public Builder baseArrayLayer(int baseArrayLayer) {
			if(baseArrayLayer >= descriptor.layerCount()) {
				throw new IllegalArgumentException(String.format("Invalid base array: %d/%d", baseArrayLayer, descriptor.layerCount()));
			}
			this.baseArrayLayer = zeroOrMore(baseArrayLayer);
			return this;
		}

		/**
		 * Sets the number of array layers.
		 * @param layerCount Number of array layers
		 * @throws IllegalArgumentException if the number of layers is higher than the parent
		 */
		public Builder layerCount(int layerCount) {
			if(layerCount > descriptor.layerCount()) {
				throw new IllegalArgumentException(String.format("Invalid layer count: count=%d parent=%d", layerCount, descriptor.layerCount()));
			}
			this.layerCount = oneOrMore(layerCount);
			return this;
		}

		/**
		 * Constructs this sub-resource.
		 * @return New sub-resource
		 */
		public SubResource build() {
			// Init image aspects if not explicitly specified
			if(aspects.isEmpty()) {
				aspects = descriptor.aspects();
			}

			// Private implementation
			record DefaultSubResource(Set<VkImageAspect> aspects, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) implements SubResource {
				@Override
				public String toString() {
					return new ToStringBuilder(this)
							.append(aspects)
							.append(String.format("levels %d/%d", mipLevel, levelCount))
							.append(String.format("layers %d/%d", baseArrayLayer, layerCount))
							.build();
				}
			}

			// Create sub-resource
			return new DefaultSubResource(aspects, mipLevel, levelCount, baseArrayLayer, layerCount);
		}
	}
}
