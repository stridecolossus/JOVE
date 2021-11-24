package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.lib.util.Check;

/**
 * An <i>image descriptor</i> specifies the properties of an image.
 * @author Sarge
 */
public record ImageDescriptor(VkImageType type, VkFormat format, ImageExtents extents, Set<VkImageAspect> aspects, int levelCount, int layerCount) implements SubResource {
	// Valid image aspect combinations
	private static final Collection<Set<VkImageAspect>> VALID_ASPECTS = List.of(
			Set.of(VkImageAspect.COLOR),
			Set.of(VkImageAspect.DEPTH),
			Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL)
	);

	/**
	 * Constructor.
	 * @param type			Image type
	 * @param format		Format
	 * @param extents		Extents
	 * @param aspects		Image aspect(s)
	 * @param levelCount	Number of mip levels
	 * @param layerCount	Number of array layers
	 * @throws IllegalArgumentException if the image aspects is empty or is an invalid combination
	 * @throws IllegalArgumentException if the extents are invalid for the given image type
	 */
	public ImageDescriptor {
		// Validate
		Check.notNull(type);
		Check.notNull(format);
		Check.notNull(extents);
		aspects = Set.copyOf(notEmpty(aspects));
		Check.oneOrMore(levelCount);
		Check.oneOrMore(layerCount);

		// Validate extents
		final boolean valid = switch(type) {
			case IMAGE_TYPE_1D -> (extents.size().height() == 1) && (extents.depth() == 1);
			case IMAGE_TYPE_2D -> extents.depth() == 1;
			case IMAGE_TYPE_3D -> true;
		};
		if(!valid) {
			throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
		}

		// Validate array layers
		if((type == VkImageType.IMAGE_TYPE_3D) && (layerCount != 1)) {
			throw new IllegalArgumentException("Array layers must be one for a 3D image");
		}

		// Validate image aspects
		if(!VALID_ASPECTS.contains(aspects)) throw new IllegalArgumentException("Invalid image aspects: " + aspects);

		// TODO - validate format against aspects, e.g. D32_FLOAT is not stencil, D32_FLOAT_S8_UINT has stencil
	}

	@Override
	public int mipLevel() {
		return 0;
	}

	@Override
	public int baseArrayLayer() {
		return 0;
	}

	/**
	 * Builder for an image descriptor.
	 */
	public static class Builder {
		private VkImageType type = VkImageType.IMAGE_TYPE_2D;
		private VkFormat format;
		private ImageExtents extents;
		private final Set<VkImageAspect> aspects = new HashSet<>();
		private int levels = 1;
		private int layers = 1;

		/**
		 * Sets the image type (default is {@link VkImageType#IMAGE_TYPE_2D}).
		 * @param type Image type
		 */
		public Builder type(VkImageType type) {
			this.type = notNull(type);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Image format
		 */
		public Builder format(VkFormat format) {
			this.format = notNull(format);
			return this;
		}

		/**
		 * Sets the image extents.
		 * @param extents Image extents
		 */
		public Builder extents(ImageExtents extents) {
			this.extents = notNull(extents);
			return this;
		}

		/**
		 * Adds an image aspect.
		 * @param aspect Image aspect
		 */
		public Builder aspect(VkImageAspect aspect) {
			aspects.add(notNull(aspect));
			return this;
		}

		/**
		 * Sets the number of mip levels (default is one).
		 * @param levels Number of mip levels
		 */
		public Builder mipLevels(int levels) {
			this.levels = oneOrMore(levels);
			return this;
		}

		/**
		 * Sets the number of array levels (default is one).
		 * @param levels Number of array levels
		 */
		public Builder arrayLayers(int layers) {
			this.layers = oneOrMore(layers);
			return this;
		}

		/**
		 * Constructs this descriptor.
		 * @return New image descriptor
		 */
		public ImageDescriptor build() {
			return new ImageDescriptor(type, format, extents, aspects, levels, layers);
		}
	}
}
