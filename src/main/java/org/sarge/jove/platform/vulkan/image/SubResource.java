package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;

/**
 * An <i>image sub-resource</i> defines a subset of the image aspects, mip levels and array layers of an image.
 * TODO
 * @author Sarge
 */
public interface SubResource {
	/**
	 * Special case identifier indicating the <i>remaining</i> number of mip levels or array layers.
	 */
	int REMAINING = (~0);

	/**
	 * @return Image aspects
	 */
	Set<VkImageAspect> aspects();

	/**
	 * @return Base level
	 */
	int mipLevel();

	/**
	 * @return Number of levels
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
	 * @param res Sub-resource
	 * @return New sub-resource range descriptor
	 */
	static VkImageSubresourceRange toRange(SubResource res) {
		final var range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.mask(res.aspects());
		range.baseMipLevel = res.mipLevel();
		range.levelCount = res.levelCount();
		range.baseArrayLayer = res.baseArrayLayer();
		range.layerCount = res.layerCount();
		return range;
	}

	/**
	 * @param res Sub-resource
	 * @return New sub-resource layers descriptor
	 */
	static VkImageSubresourceLayers toLayers(SubResource res) {
		final var layers = new VkImageSubresourceLayers();
		layers.aspectMask = IntegerEnumeration.mask(res.aspects());
		layers.mipLevel = res.mipLevel();
		layers.baseArrayLayer = res.baseArrayLayer();
		layers.layerCount = res.layerCount();
		return layers;
	}

	/**
	 * Builder for an image sub-resource.
	 */
	class Builder {
		private final ImageDescriptor descriptor;
		private final Set<VkImageAspect> aspects = new HashSet<>();
		private int mipLevel;
		private int levelCount = 1;
		private int baseArrayLayer;
		private int layerCount = 1;

		/**
		 * Constructor.
		 * @param parent Parent image descriptor
		 */
		public Builder(ImageDescriptor descriptor) {
			this.descriptor = notNull(descriptor);
		}

		// TODO
		// - setters require implicit ordering to init levelCount before mipLevel (ditto for array layers)
		// - better to init this builder from the parent descriptor?
		// - but then would generally want to remove aspects, not add them
		// - currently does not validate that a sub-resource is valid for the parent in the various use-cases, e.g. views
		// - better as an abstract class? with adapter for the image descriptor case, i.e. descriptor does not extend?
		// - static helpers for the toXXX() is also nasty

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
		 * Adds a collection of image aspects.
		 * @param aspects Image aspects
		 * @throws IllegalArgumentException if any of the given aspects is not a member of the parent
		 */
		public Builder aspects(Collection<VkImageAspect> aspects) {
			aspects.forEach(this::aspect);
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
			if(levelCount > descriptor.levelCount()) {
				throw new IllegalArgumentException(String.format("Invalid level count: count=%d parent=%d", levelCount, descriptor.levelCount()));
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
			if(layerCount > descriptor.layerCount()) {
				throw new IllegalArgumentException(String.format("Invalid layer count: count=%d parent=%d", layerCount, descriptor.layerCount()));
			}
			this.layerCount = oneOrMore(layerCount);
			return this;
		}

		/**
		 * Default implementation.
		 */
		private record DefaultSubResource(Set<VkImageAspect> aspects, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) implements SubResource {
			@Override
			public String toString() {
				return new ToStringBuilder(this)
						.append(aspects)
						.append(String.format("levels %d/%d", mipLevel, levelCount))
						.append(String.format("layers %d/%d", baseArrayLayer, layerCount))
						.build();
			}
		}

		/**
		 * Constructs this sub-resource.
		 * @return New sub-resource
		 * @throws IllegalArgumentException if the aspect mask is empty
		 */
		public SubResource build() {
			if(aspects.isEmpty()) throw new IllegalArgumentException("Aspect mask cannot be empty");
			return new DefaultSubResource(aspects, mipLevel, levelCount, baseArrayLayer, layerCount);
		}
	}
}
