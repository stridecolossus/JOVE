package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

/**
 * A Vulkan <i>image</i> is a texture or data image stored on the hardware.
 * @author Sarge
 */
public interface Image extends NativeObject {
	/**
	 * Number of array layers for a cube-map image.
	 */
	int CUBEMAP_ARRAY_LAYERS = 6;

	/**
	 * @return Descriptor for this image
	 */
	Descriptor descriptor();

	/**
	 * An <i>image descriptor</i> specifies the properties of this image.
	 * <p>
	 * Note that an image descriptor is-a {@link SubResource} with default (i.e. zero) values for the {@link #mipLevel()} and {@link #baseArrayLayer()} properties.
	 */
	record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspect> aspects, int levelCount, int layerCount) implements SubResource {
		// Valid image aspect combinations
		private static final Collection<Set<VkImageAspect>> ASPECTS = List.of(
				Set.of(VkImageAspect.COLOR),
				Set.of(VkImageAspect.DEPTH),
				Set.of(VkImageAspect.STENCIL),
				Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL)
		);
		// TODO - multi-planar, others, e.g. planes?

		/**
		 * Constructor.
		 * @param type			Image type
		 * @param format		Format
		 * @param extents		Image extents
		 * @param aspects		Image aspect(s)
		 * @param levelCount	Number of mip levels
		 * @param layerCount	Number of array layers
		 * @throws IllegalArgumentException if {@link #aspects} is empty or is an invalid combination
		 * @throws IllegalArgumentException if {@link #extents} is invalid for the given image {@link #type}
		 * @see Image#checkOffset(Set)
		 */
		public Descriptor {
			// Validate
			requireNonNull(type);
			requireNonNull(format);
			requireNonNull(extents);
			aspects = Set.copyOf(aspects);
			requireOneOrMore(levelCount);
			requireOneOrMore(layerCount);

			// Validate extents
			if(!extents.isValid(type)) {
				throw new IllegalArgumentException(String.format("Invalid extents for image: type=%s extents=%s", type, extents));
			}

			// Validate array layers
			if((type == VkImageType.THREE_D) && (layerCount != 1)) {
				throw new IllegalArgumentException("Array layers must be one for a 3D image");
			}

			// Validate image aspects
			if(!ASPECTS.contains(aspects)) {
				throw new IllegalArgumentException("Invalid image aspects: " + aspects);
			}
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
			 * Sets the image type (default is a 2D image).
			 * @param type Image type
			 */
			public Builder type(VkImageType type) {
				this.type = requireNonNull(type);
				return this;
			}

			/**
			 * Sets the image format.
			 * @param format Image format
			 */
			public Builder format(VkFormat format) {
				this.format = requireNonNull(format);
				return this;
			}

			/**
			 * Sets the image extents.
			 * @param size Image dimensions
			 */
			public Builder extents(Extents extents) {
				this.extents = requireNonNull(extents);
				return this;
			}

			/**
			 * Convenience setter for the extents of a 2D image.
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
				aspects.add(requireNonNull(aspect));
				return this;
			}

			/**
			 * Sets the number of mip levels (default is one).
			 * @param levels Number of mip levels
			 */
			public Builder mipLevels(int levels) {
				this.levels = requireOneOrMore(levels);
				return this;
			}

			/**
			 * Sets the number of array levels (default is one).
			 * @param levels Number of array levels
			 */
			public Builder arrayLayers(int layers) {
				this.layers = requireOneOrMore(layers);
				return this;
			}

			/**
			 * Constructs this descriptor.
			 * @return New image descriptor
			 * @see Descriptor#Descriptor(VkImageType, VkFormat, Extents, Set, int, int)
			 */
			public Descriptor build() {
				return new Descriptor(type, format, extents, aspects, levels, layers);
			}
		}
	}
}

