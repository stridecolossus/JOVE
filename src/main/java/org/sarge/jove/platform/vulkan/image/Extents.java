package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;

/**
 * An <i>extents</i> composes the size and depth of an image.
 * @author Sarge
 */
public record Extents(Dimensions size, int depth) {
	/**
	 * Extents for an image with zero dimensions.
	 */
	public static final Extents ZERO = new Extents(new Dimensions(0, 0), 0);

	/**
	 * Constructor.
	 * @param dim		Image dimensions
	 * @param depth		Depth
	 */
	public Extents {
		requireNonNull(size);
		requireZeroOrMore(depth);
	}

	/**
	 * Convenience constructor for a 2D image.
	 * @param size Image extents
	 */
	public Extents(Dimensions size) {
		this(size, 1);
	}

	/**
	 * @return Whether these extents are valid for the given type of image
	 */
	public boolean isValid(VkImageType type) {
		return switch(type) {
			case TYPE_1D -> (size.height() == 1) && (depth == 1);
			case TYPE_2D -> depth == 1;
			case TYPE_3D -> true;
			default -> throw new RuntimeException();
		};
	}

	/**
	 * @return 3D extents
	 */
	public VkExtent3D toExtent() {
		final var extent = new VkExtent3D();
		extent.width = size.width();
		extent.height = size.height();
		extent.depth = depth;
		return extent;
	}

	/**
	 * @return 3D offsets
	 */
	public VkOffset3D toOffset() {
		final var offset = new VkOffset3D();
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
			requireOneOrMore(level);
			final int w = mip(size.width(), level);
			final int h = mip(size.height(), level);
			return new Extents(new Dimensions(w, h), depth);
		}
	}

	private static int mip(int value, int level) {
		return Math.max(1, value >> level);
	}
}
