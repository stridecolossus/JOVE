package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Validation.*;

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
	 * Creates a version from the given native integer.
	 * @param version Native integer
	 * @return Version
	 */
	public static Version of(int version) {
		final int major = version >> 22;
		final int minor = (version >> 12) & 0x3FF;
		final int patch = version & 0xFFF;
		return new Version(major, minor, patch);
	}

	/**
	 * Constructor.
	 */
	public Version {
		requireOneOrMore(major);
		requireZeroOrMore(minor);
		requireZeroOrMore(patch);
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
		return Integer.compare(this.toInteger(), that.toInteger());
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d", major, minor, patch);
	}
}
