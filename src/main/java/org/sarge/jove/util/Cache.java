package org.sarge.jove.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Cache of resources.
 * @author Sarge
 * @param <T> Resource type
 */
public class Cache<T> {
	/**
	 * Cache limiting policy.
	 */
	public static interface CacheLimit {
		/**
		 * Applies this limit to the given cache.
		 * @param cache Cache to test
		 * @return Whether the given cache is full
		 */
		boolean isFull( Cache<?> cache );
	}

	/**
	 * Cache entry.
	 */
	public static class CacheEntry<T> {
		private T value;
		private long time;
		private int count;

		protected CacheEntry() {
		}

		/**
		 * @return Last access time
		 */
		public long getTime() {
			return time;
		}

		/**
		 * @return Number of accesses
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Updates usage stats for this entry.
		 */
		private void update() {
			time = System.currentTimeMillis();
			++count;
		}
	}

	/**
	 * Thrown if this cache is full.
	 */
	public static class CacheExhaustedException extends Exception {
		private CacheExhaustedException() {
			super( "Cache exhausted" );
		}
	}

	// Cache
	private final Map<String, CacheEntry<T>> cache = new HashMap<>();

	// Config
	private CacheLimit limit;
	private PruneStrategy strategy;
	private int pruneSize = 1;

	// Stats
	// TODO - total memory footprint?

	/**
	 * Sets the limiting policy for this cache.
	 * @param limit Limiting policy or <tt>null</tt> if unlimited
	 */
	public void setLimit( CacheLimit limit ) {
		this.limit = limit;
	}

	/**
	 * Sets the pruning strategy to be used by this cache when it is full.
	 * @param strategy Pruning strategy or <tt>null</tt> if none
	 */
	public void setPruningStrategy( PruneStrategy strategy ) {
		this.strategy = strategy;
	}

	/**
	 * Sets the number of entries to be pruned when the cache is full.
	 * @param pruneSize Number of entries to be pruned
	 */
	public void setPruneSize( int pruneSize ) {
		Check.oneOrMore( pruneSize );
		this.pruneSize = pruneSize;
	}

	/**
	 * @return Cache entries ordered by resource name
	 */
	public final Map<String, CacheEntry<T>> getCache() {
		return Collections.unmodifiableMap( cache );
	}

	/**
	 * Retrieves a resource from this cache.
	 * @param key Resource name
	 * @return Specified resource or <tt>null</tt> if not cached
	 */
	public T get( String key ) {
		final CacheEntry<T> entry = cache.get( key );
		if( entry == null ) {
			return null;
		}
		else {
			entry.update();
			return entry.value;
		}
	}

	/**
	 * Adds a resource to this cache pruning stale entries as necessary.
	 * @see #setPruningStrategy(PruneStrategy)
	 * @param key		Resource name
	 * @param res		Resource
	 * @throws CacheExhaustedException if this cache is full
	 */
	public void add( String key, T res ) throws CacheExhaustedException {
		// Purge cache if limit reached
		if( limit != null ) {
			if( limit.isFull( this ) ) {
				if( strategy == null ) throw new CacheExhaustedException();
				prune();
			}
			assert !limit.isFull( this );
		}

		// Load resource and created new cache entry
		final CacheEntry<T> entry = new CacheEntry<>();
		entry.value = res;
		cache.put( key, entry );

		// Update entry stats
		entry.update();
	}

	/**
	 * Removes a resource from this cache.
	 * @param key Resource name
	 */
	public void remove( String key ) {
		cache.remove( key );
	}

	/**
	 * Empties this cache.
	 */
	public void clear() {
		cache.clear();
	}

	/**
	 * Used during pruning.
	 */
	private static class Score implements Comparable<Score> {
		private long score;
		private String path;

		@Override
		public int compareTo( Score that ) {
			if( this.score < that.score ) {
				return -1;
			}
			else {
				return +1;
			}
		}
	}

	/**
	 * Prunes this cache.
	 */
	protected void prune() {
		// Calculate score for each entry
		final LinkedList<Score> scores = new LinkedList<>();
		for( Entry<String, CacheEntry<T>> e : cache.entrySet() ) {
			final Score sc = new Score();
			sc.path = e.getKey();
			sc.score = strategy.evaluate( e.getValue() );
			scores.add( sc );
		}

		// Order by lowest score
		Collections.sort( scores );

		// Prune entries
		for( int n = 0; n < pruneSize; ++n ) {
			final Score sc = scores.poll();
			cache.remove( sc.path );
			if( cache.isEmpty() ) break;
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
