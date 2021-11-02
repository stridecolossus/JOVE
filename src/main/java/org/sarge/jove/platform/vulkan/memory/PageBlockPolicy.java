package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Sarge
 */
public class PageBlockPolicy implements BlockPolicy {
	private final long page;
	private final long min;

	/**
	 * Constructor.
	 * @param page 		Page size (bytes)
	 * @param min		Minimum number of pages
	 */
	public PageBlockPolicy(long page, int min) {
		this.page = oneOrMore(page);
		this.min = oneOrMore(min) * page;
	}

	/**
	 * Constructor.
	 * @param page Page size (bytes)
	 */
	public PageBlockPolicy(long page) {
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
