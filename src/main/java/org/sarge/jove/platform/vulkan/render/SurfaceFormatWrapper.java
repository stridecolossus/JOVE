package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.sarge.jove.platform.vulkan.*;

/**
 * A <i>surface format wrapper</i> is a convenience adapter for a Vulkan surface format that can be compared for equality.
 * @author Sarge
 */
public class SurfaceFormatWrapper extends VkSurfaceFormatKHR {
	/**
	 * Constructor.
	 * @param format	Image format
	 * @param space		Colour space
	 */
	public SurfaceFormatWrapper(VkFormat format, VkColorSpaceKHR space) {
		this.format = requireNonNull(format);
		this.colorSpace = requireNonNull(space);
	}

	/**
	 * Copy constructor.
	 * @param that Surface format to copy
	 */
	public SurfaceFormatWrapper(VkSurfaceFormatKHR that) {
		this(that.format, that.colorSpace);
	}

	@Override
	public int hashCode() {
		return Objects.hash(format, colorSpace);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof VkSurfaceFormatKHR that) &&
				(this.format == that.format) &&
				(this.colorSpace == that.colorSpace);
	}

	@Override
	public String toString() {
		return String.format("SurfaceFormat[format=%s space=%s]", format, colorSpace);
	}
}
