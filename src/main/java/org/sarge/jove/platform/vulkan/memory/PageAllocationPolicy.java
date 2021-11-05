package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>page allocation policy</i> quantises allocation requests to a given <i>page</i> size.
 * @author Sarge
 */
public class PageAllocationPolicy implements AllocationPolicy {
	private final long page;
	private final long min;

	/**
	 * Constructor.
	 * @param page 		Page size (bytes)
	 * @param min		Minimum number of pages
	 */
	public PageAllocationPolicy(long page, int min) {
		this.page = oneOrMore(page);
		this.min = oneOrMore(min) * page;
	}

	/**
	 * Constructor.
	 * @param page Page size (bytes)
	 */
	public PageAllocationPolicy(long page) {
		this(page, 1);
	}

	@Override
	public long apply(long size, long current) {
		final long num = 1 + ((size - 1) / page);
		return Math.max(min, num * page);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("page", page)
				.append("min", min)
				.build();
	}
}
