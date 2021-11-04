package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkExtent3D;
import org.sarge.lib.util.Check;

/**
 * An <i>image extents</i> defines the extents of a Vulkan image.
 * @see VkExtent3D
 * @author Sarge
 */
public record ImageExtents(Dimensions dimensions, int depth) {
	/**
	 * Constructor.
	 * @param dim		Image dimensions
	 * @param depth		Depth
	 */
	public ImageExtents {
		Check.notNull(dimensions);
		Check.oneOrMore(depth);
	}

	/**
	 * Convenience constructor for a 2D image.
	 */
	public ImageExtents(int width, int height) {
		this(new Dimensions(width, height));
	}

	/**
	 * Convenience constructor for a 2D image.
	 */
	public ImageExtents(Dimensions dim) {
		this(dim, 1);
	}

	/**
	 * @return Vulkan image extents descriptor
	 */
	public VkExtent3D toExtent3D() {
		final VkExtent3D extent = new VkExtent3D();
		extent.width = dimensions.width();
		extent.height = dimensions.height();
		extent.depth = depth;
		return extent;
	}
}
