package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor.Extents;
import org.sarge.lib.util.Check;

/**
 * An <i>image descriptor</i> specifies the static properties of an image.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record ImageDescriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspect> aspects, int levelCount, int layerCount) implements SubResource {
	// Valid image aspect combinations
	private static final Collection<Set<VkImageAspect>> VALID_ASPECTS = List.of(
			Set.of(VkImageAspect.COLOR),
			Set.of(VkImageAspect.DEPTH),
			Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL)
	);

	/**
	 * Image extents.
	 */
	public record Extents(Dimensions size, int depth) {
		/**
		 * Constructor.
		 * @param dim		Image dimensions
		 * @param depth		Depth
		 */
		public Extents {
			Check.notNull(size);
			Check.range(depth, 1, 3);
		}

		/**
		 * Constructor for a 2D image.
		 * @param size Image extents
		 */
		public Extents(Dimensions size) {
			this(size, 1);
		}

		/**
		 * @return Whether these extents are valid for the given type of image
		 */
		private boolean isValid(VkImageType type) {
			return switch(type) {
				case ONE_D -> (size.height() == 1) && (depth == 1);
				case TWO_D -> depth == 1;
				case THREE_D -> true;
			};
		}

		/**
		 * Converts to Vulkan 3D extents.
		 * @return 3D extents
		 */
		public VkExtent3D toExtent() {
			final VkExtent3D extent = new VkExtent3D();
			extent.width = size.width();
			extent.height = size.height();
			extent.depth = depth;
			return extent;
		}

		/**
		 * Converts to Vulkan offsets.
		 * @return Offsets
		 */
		public VkOffset3D toOffset() {
			final VkOffset3D offset = new VkOffset3D();
			offset.x = size.width();
			offset.y = size.height();
			offset.z = depth;
			return offset;
		}

		/**
		 * Calculates the image extents for the given MIP level.
		 * @param level MIP level
		 * @return MIP extents
		 */
		public Extents mip(int level) {
			if(level == 0) {
				return this;
			}
			else {
				Check.oneOrMore(level);
				final int w = mip(size.width(), level);
				final int h = mip(size.height(), level);
				return new Extents(new Dimensions(w, h), depth);
			}
		}

		private static int mip(int value, int level) {
			return Math.max(1, value >> level);
		}
	}

	/**
	 * Constructor.
	 * @param type			Image type
	 * @param format		Format
	 * @param size			Image dimensions
	 * @param depth			Image depth
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
		if(extents.isValid(type)) {
			throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
		}

		// Validate array layers
		if((type == VkImageType.THREE_D) && (layerCount != 1)) {
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
		private VkImageType type = VkImageType.TWO_D;
		private VkFormat format;
		private Extents extents;
		private final Set<VkImageAspect> aspects = new HashSet<>();
		private int levels = 1;
		private int layers = 1;

		/**
		 * Sets the image type (default is {@link VkImageType#TWO_D}).
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
		 * @param size Image dimensions
		 */
		public Builder extents(Extents extents) {
			this.extents = notNull(extents);
			return this;
		}

		/**
		 * Sets the image extents.
		 * @param size Image dimensions
		 */
		public Builder extents(Dimensions size) {
			return extents(new Extents(size));
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
