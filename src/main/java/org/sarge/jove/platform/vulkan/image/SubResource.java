package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;

/**
 * An <i>image sub-resource</i> defines a subset of the image aspects, mip levels and array layers of an image.
 * @author Sarge
 */
public record SubResource(Set<VkImageAspect> aspects, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) {
	/**
	 * Special case identifier indicating the <i>remaining</i> number of mip levels or array layers.
	 */
	public static final int REMAINING = (~0);

	/**
	 * @param res Sub-resource
	 * @return New sub-resource range descriptor
	 */
	public VkImageSubresourceRange toRange() {
		final var range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.mask(aspects);
		range.baseMipLevel = mipLevel;
		range.levelCount = levelCount;
		range.baseArrayLayer = baseArrayLayer;
		range.layerCount = layerCount;
		return range;
	}

	/**
	 * @return New sub-resource layers descriptor
	 */
	VkImageSubresourceLayers toLayers() {
		final var layers = new VkImageSubresourceLayers();
		layers.aspectMask = IntegerEnumeration.mask(aspects);
		layers.mipLevel = mipLevel;
		layers.baseArrayLayer = baseArrayLayer;
		layers.layerCount = layerCount;
		return layers;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(aspects)
				.append(String.format("levels %d/%d", mipLevel, levelCount))
				.append(String.format("layers %d/%d", baseArrayLayer, layerCount))
				.build();
	}

	/**
	 * Builder for an image sub-resource.
	 */
	public static class Builder {
		private final ImageDescriptor descriptor;
		private Set<VkImageAspect> aspects = new HashSet<>();
		private int mipLevel;
		private int levelCount;
		private int baseArrayLayer;
		private int layerCount;

		/**
		 * Constructor.
		 * @param parent Parent image descriptor
		 */
		public Builder(ImageDescriptor descriptor) {
			this.descriptor = notNull(descriptor);
			this.levelCount = descriptor.levels();
			this.layerCount = descriptor.layers();
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
		 * @throws IllegalArgumentException if the mip level exceeds the number of levels
		 */
		public Builder mipLevel(int mipLevel) {
			if(mipLevel >= levelCount) {
				throw new IllegalArgumentException(String.format("Invalid mip level: %d/%d", mipLevel, levelCount));
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
			if(levelCount > descriptor.levels()) {
				throw new IllegalArgumentException(String.format("Invalid level count: count=%d parent=%d", levelCount, descriptor.levels()));
			}
			this.levelCount = oneOrMore(levelCount);
			return this;
		}

		/**
		 * Sets the starting array layer.
		 * @param baseArrayLayer Starting array layer
		 * @throws IllegalArgumentException if the base layer exceeds the number of layers
		 */
		public Builder baseArrayLayer(int baseArrayLayer) {
			if(baseArrayLayer >= layerCount) {
				throw new IllegalArgumentException(String.format("Invalid base array: %d/%d", baseArrayLayer, layerCount));
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
			if(layerCount > descriptor.layers()) {
				throw new IllegalArgumentException(String.format("Invalid layer count: count=%d parent=%d", layerCount, descriptor.layers()));
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

			// Create sub-resource
			return new SubResource(aspects, mipLevel, levelCount, baseArrayLayer, layerCount);
		}
	}
}
