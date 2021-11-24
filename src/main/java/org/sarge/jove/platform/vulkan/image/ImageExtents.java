package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkExtent3D;
import org.sarge.jove.platform.vulkan.VkOffset3D;
import org.sarge.lib.util.Check;

/**
 * An <i>image extents</i> defines the extents of a Vulkan image.
 * @see VkExtent3D
 * @author Sarge
 */
public record ImageExtents(Dimensions size, int depth) {
	/**
	 * Constructor.
	 * @param dim		Image dimensions
	 * @param depth		Depth
	 */
	public ImageExtents {
		Check.notNull(size);
		Check.zeroOrMore(depth);
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
	public ImageExtents(Dimensions size) {
		this(size, 1);
	}

	/**
	 * Converts to Vulkan extents.
	 * @return Extents
	 */
	public VkExtent3D extents() {
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
	public VkOffset3D offsets() {
		final VkOffset3D offset = new VkOffset3D();
		offset.x = size.width();
		offset.y = size.height();
		offset.z = depth;
		return offset;
	}
}
