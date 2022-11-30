package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.*;

/**
 * Vulkan version number.
 * @author Sarge
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {
	/**
	 * Default version number.
	 */
	public static final Version DEFAULT = new Version(1, 0, 0);

	/**
	 * Constructor.
	 */
	public Version {
		oneOrMore(major);
		zeroOrMore(minor);
		zeroOrMore(patch);
	}

	/**
	 * @return Packed version integer
	 * @see Vulkan header {@code VK_MAKE_VERSION} macro
	 */
	public int toInteger() {
        return (int) ((long) major << 22) | (minor << 12) | patch;
	}

	@Override
	public int compareTo(Version that) {
		return this.toInteger() - that.toInteger();
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d", major, minor, patch);
	}
}
