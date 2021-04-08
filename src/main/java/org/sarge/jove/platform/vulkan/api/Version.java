package org.sarge.jove.platform.vulkan.api;

import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

/**
 * Vulkan version.
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {
	private static final char DELIMITER = '.';

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
		final StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append(DELIMITER);
		sb.append(minor);
		sb.append(DELIMITER);
		sb.append(patch);
		return sb.toString();
	}
}
