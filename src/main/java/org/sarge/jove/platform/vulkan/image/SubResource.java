package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageSubresourceLayers;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.lib.util.Check;

/**
 * An <i>image sub-resource</i> defines a subset of the image aspects, mip levels and array layers of an image.
 * @author Sarge
 */
public record SubResource(Set<VkImageAspect> mask, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) {
//public record SubResource(Descriptor descriptor, Set<VkImageAspect> mask, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) {
	/**
	 * Special case identifier indicating the <i>remaining</i> number of mip levels or array layers.
	 */
	public static final int REMAINING = (~0);

	/**
	 * Creates a top-level sub-resource for the given descriptor, i.e. all aspects, mip levels and array layers.
	 * @param descriptor Image descriptor
	 * @return New sub-resource
	 */
	public static SubResource of(Descriptor descriptor) {
		//return new Builder(descriptor).build();

		return null;
	}

	/**
	 * Helper - Creates or validates a sub-resource for the given image descriptor.
	 * <p>
	 * This helper is intended for cases where a sub-resource is required by the API which can also be configured by the application.
	 * For example {@link View.Builder#subresource(SubResource)}.
	 * <p>
	 * If <i>subresource</i> is {@code null} this method delegates to {@link #of(Descriptor)} to create a new top-level sub-resource.
	 * Otherwise the sub-resource is validated by checking that it was created from the given descriptor.
	 * <p>
	 * @param descriptor		Image descriptor
	 * @param subresource		Optional sub-resource
	 * @return Sub-resource
	 * @throws IllegalStateException if the sub-resource was not created from the given image descriptor
	 */
 	public static SubResource of(Descriptor descriptor, SubResource subresource) {
		if(subresource == null) {
			return of(descriptor);
		}
		else {
//			if(subresource.descriptor != descriptor) throw new IllegalStateException("Invalid sub-resource for image descriptor");
			return subresource;
		}
	}

	/**
	 * Constructor.
	 * @param descriptor			Image descriptor
	 * @param mask					Aspect mask
	 * @param mipLevel				Starting mip level
	 * @param levelCount			Number of mip levels
	 * @param baseArrayLayer		Starting array layer
	 * @param layerCount			Number of array layers
	 * @throws IllegalArgumentException if the aspect mask, mip levels or array layers are not a subset of the descriptor
	 */
	public SubResource {
		// Validate
//		Check.notNull(descriptor);
		mask = Set.copyOf(notEmpty(mask));
		Check.zeroOrMore(mipLevel);
		Check.oneOrMore(levelCount);
		Check.zeroOrMore(baseArrayLayer);
		Check.oneOrMore(layerCount);

//		// Check aspects is a subset
//		if(!descriptor.aspects().containsAll(mask)) throw new IllegalArgumentException("Aspect mask must be a subset of the descriptor");
//
//		// Check mip levels / layers is subset
//		Check.range(mipLevel + levelCount, 0, descriptor.levels());
//		Check.range(baseArrayLayer + layerCount, 0, descriptor.layers());
	}

	/**
	 * @return New sub-resource range descriptor
	 */
	public VkImageSubresourceRange toRange() {
		final var range = new VkImageSubresourceRange();
		range.aspectMask = IntegerEnumeration.mask(mask);
		range.baseMipLevel = mipLevel;
		range.levelCount = levelCount;
		range.baseArrayLayer = baseArrayLayer;
		range.layerCount = layerCount;
		return range;
	}

	/**
	 * @return New sub-resource layers descriptor
	 */
	public VkImageSubresourceLayers toLayers() {
		final var layers = new VkImageSubresourceLayers();
		layers.aspectMask = IntegerEnumeration.mask(mask);
		layers.mipLevel = mipLevel;
		layers.baseArrayLayer = baseArrayLayer;
		layers.layerCount = layerCount;
		return layers;
	}

	/**
	 * Builder for an image sub-resource.
	 */
	public static class Builder {
//		private final Descriptor descriptor;
		private Set<VkImageAspect> mask;
		private int mipLevel;
		private int levelCount;
		private int baseArrayLayer;
		private int layerCount;

//		/**
//		 * Constructor.
//		 * @param descriptor Image descriptor
//		 */
//		public Builder(Descriptor descriptor) {
//			this.descriptor = notNull(descriptor);
//			this.levelCount = descriptor.levels();
//			this.layerCount = descriptor.layers();
//			mask(descriptor.aspects());
//		}

		/**
		 * Sets the aspect mask.
		 * @param aspect Aspect mask
		 */
		public Builder mask(Set<VkImageAspect> mask) {
			this.mask = new HashSet<>(mask);
			return this;
		}

		/**
		 * Helper - <b>Removes</b> the specified image aspect from this sub-resource mask.
		 * @param aspect Aspect to remove
		 * @throws IllegalArgumentException if the aspect is not present in the descriptor
		 */
		public Builder remove(VkImageAspect aspect) {
			final boolean removed = mask.remove(aspect);
			if(!removed) throw new IllegalArgumentException("Image does not contain aspect: " + aspect);
			return this;
		}

		/**
		 * Sets the starting mip level.
		 * @param mipLevel Starting mip level
		 */
		public Builder mipLevel(int mipLevel) {
			this.mipLevel = zeroOrMore(mipLevel);
			return this;
		}

		/**
		 * Sets the number of mip levels.
		 * @param levelCount Number of mip levels
		 */
		public Builder levelCount(int levelCount) {
			this.levelCount = zeroOrMore(levelCount);
			return this;
		}

		/**
		 * Sets the starting array layer.
		 * @param baseArrayLayer Starting array layer
		 */
		public Builder baseArrayLayer(int baseArrayLayer) {
			this.baseArrayLayer = zeroOrMore(baseArrayLayer);
			return this;
		}

		/**
		 * Sets the number of array layers.
		 * @param layerCount Number of array layers
		 */
		public Builder layerCount(int layerCount) {
			this.layerCount = zeroOrMore(layerCount);
			return this;
		}

		/**
		 * Constructs this sub-resource.
		 * @return New sub-resource
		 * @throws IllegalArgumentException if the sub-resource is invalid
		 * @see SubResource#SubResource(Descriptor, Set, int, int, int, int)
		 */
		public SubResource build() {
//			return new SubResource(descriptor, mask, mipLevel, levelCount, baseArrayLayer, layerCount);
			return new SubResource(mask, mipLevel, levelCount, baseArrayLayer, layerCount);
		}
	}
}
