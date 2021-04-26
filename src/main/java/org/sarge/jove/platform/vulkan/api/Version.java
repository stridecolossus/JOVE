package org.sarge.jove.platform.vulkan.api;

import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

/**
 * Vulkan version.
 * @author Sarge
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {
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
	 */
	public int toInteger() {
		// TODO - could overflow
        return (major << 22) | (minor << 12) | patch;
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
