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
	 * @param major
	 * @param minor
	 * @param patch
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
        return (int) compact();
	}

	private long compact() {
        return (major << 22) | (minor << 12) | patch;
	}

	@Override
	public int compareTo(Version that) {
		return (int) (this.compact() - that.compact());
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d", major, minor, patch);
	}
}
