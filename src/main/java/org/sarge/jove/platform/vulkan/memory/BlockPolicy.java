package org.sarge.jove.platform.vulkan.memory;

import org.sarge.lib.util.Check;

/**
 * A <i>block policy</i> is applied to a requested memory size when allocating a new memory block.
 * @author Sarge
 */
@FunctionalInterface
public interface BlockPolicy {
	/**
	 * Calculates the size of a new memory block according to this policy.
	 * @param size			Requested memory size
	 * @param current		Current pool size
	 * @return Modified size to allocate
	 */
	long apply(long size, long current);

	/**
	 * Default policy that does not modify the requested size.
	 */
	BlockPolicy NONE = (size, current) -> size;

	/**
	 * Creates a policy for a minimal or incremental block size.
	 * @param inc Block size increment
	 * @return Literal block size policy
	 */
	static BlockPolicy literal(long inc) {
		Check.oneOrMore(inc);
		return (size, current) -> inc;
	}

	/**
	 * Creates a block size policy that expands the memory pool by the given scaling factor.
	 * @param scale Growth scalar
	 * @return Expand policy
	 * @throws IllegalArgumentException if the scaling factor is not positive
	 */
	static BlockPolicy expand(float scale) {
		if(scale <= 0) throw new IllegalArgumentException("Growth scalar must be greater-than zero");
		return (size, current) -> Math.max(size, (long) (current * scale));
	}
}
