package org.sarge.jove.util;

import org.sarge.jove.util.Cache.CacheLimit;

/**
 * Limits by number of entries.
 */
public class SizeCacheLimit implements CacheLimit {
	private final int max;

	/**
	 * Constructor.
	 * @param max Maximum number of entries
	 */
	public SizeCacheLimit( int max ) {
		// Check.oneOrMore( max );
		this.max = max;
	}

	@Override
	public boolean isFull( Cache<?> cache ) {
		return cache.getCache().size() >= max;
	}

	@Override
	public String toString() {
		return "max(" + max + ")";
	}
}
