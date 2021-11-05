package org.sarge.jove.platform.vulkan.memory;

import org.sarge.lib.util.Check;

/**
 * An <i>allocation policy</i> modifies the size of a memory request.
 * @author Sarge
 */
@FunctionalInterface
public interface AllocationPolicy {
	/**
	 * Calculates the size of a new memory according to this policy.
	 * @param size			Requested memory size
	 * @param total			Total memory
	 * @return Modified size to allocate
	 */
	long apply(long size, long total);

	/**
	 * Default policy that does not modify the requested size.
	 */
	AllocationPolicy NONE = (size, total) -> size;

	/**
	 * Creates a policy adapter that is only applied to an empty pool.
	 * @param initial Initial size
	 * @return Initial size policy
	 */
	default AllocationPolicy initial(long initial) {
		Check.oneOrMore(initial);
		return (size, total) -> {
			if(total == 0) {
				return initial;
			}
			else {
				return AllocationPolicy.this.apply(size, total);
			}
		};
	}

	/**
	 * Creates a policy for a minimal or incremental allocation size.
	 * @param inc Size increment
	 * @return Literal size policy
	 */
	static AllocationPolicy literal(long inc) {
		Check.oneOrMore(inc);
		return (size, total) -> inc;
	}

	/**
	 * Creates a policy that grows a pool by the given scaling factor.
	 * @param scale Growth scalar
	 * @return Expand policy
	 * @throws IllegalArgumentException if the scaling factor is not positive
	 */
	static AllocationPolicy expand(float scale) {
		if(scale <= 0) throw new IllegalArgumentException("Scalar must be greater-than zero");
		return (size, total) -> (long) (total * scale);
	}
}
