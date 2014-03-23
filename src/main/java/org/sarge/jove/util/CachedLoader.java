package org.sarge.jove.util;

import java.io.IOException;

import org.sarge.jove.util.Cache.CacheExhaustedException;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Adaptor for a cached loader.
 * @author Sarge
 * @param <T> Resource type
 */
public class CachedLoader<T> implements Loader<T> {
	private final Cache<T> cache;
	private final Loader<T> loader;

	/**
	 * Constructor.
	 * @param loader	Underlying loader
	 * @param cache		Resource cache
	 */
	public CachedLoader( Loader<T> loader, Cache<T> cache ) {
		Check.notNull( loader );
		Check.notNull( cache );
		if( loader instanceof CachedLoader ) throw new IllegalArgumentException( "Cannot cache a cache!" );

		this.loader = loader;
		this.cache = cache;
	}

	/**
	 * Constructor using an unlimited cache.
	 * @param loader Underlying loader
	 */
	public CachedLoader( Loader<T> loader ) {
		this( loader, new Cache<T>() );
	}

	@Override
	public T load( String path ) throws IOException {
		// Lookup from cache
		T res = cache.get( path );

		// Load resource if not cached
		if( res == null ) {
			// Delegate
			res = loader.load( path );

			// Add to cache
			try {
				cache.add( path, res );
			}
			catch( CacheExhaustedException e ) {
				throw new IOException( "Cache exhausted: " + path, e );
			}
		}

		// Return cached value
		return res;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
