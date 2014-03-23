package org.sarge.jove.util;

import org.sarge.jove.util.Cache.CacheEntry;

/**
 * Defines a strategy for pruning a {@link Cache}.
 */
public interface PruneStrategy {
	/**
	 * Prunes the oldest entries.
	 */
	PruneStrategy OLDEST = new PruneStrategy() {
		@Override
		public long evaluate( CacheEntry<?> entry ) {
			return entry.getTime();
		}
	};

	/**
	 * Prune least-used entries.
	 */
	PruneStrategy LEAST_USED = new PruneStrategy() {
		@Override
		public long evaluate( CacheEntry<?> entry ) {
			return entry.getCount();
		}
	};

	/**
	 * Calculates a <i>score</i> for the given entry according to this pruning strategy.
	 * @param entry Entry being evaluated
	 * @return Score for the given entry
	 * @see #prune()
	 */
	long evaluate( CacheEntry<?> entry );
}
