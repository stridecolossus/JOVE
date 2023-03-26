package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.util.Check;

/**
 * The <i>extents</i> specifies the size and depth of an image.
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
		Check.notNull(size);
		Check.zeroOrMore(depth);
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
		final var extent = new VkExtent3D();
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
