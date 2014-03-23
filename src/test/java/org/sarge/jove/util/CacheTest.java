package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sarge.jove.util.Cache.CacheExhaustedException;

public class CacheTest {
	private static final String KEY = "key";
	private static final String RESOURCE = "resource";

	private Cache<String> cache;

	@Rule public ExpectedException exception = ExpectedException.none();

	@Before
	public void before() throws IOException {
		cache = new Cache<>();
	}

	@Test
	public void constructor() {
		assertNotNull( cache.getCache() );
		assertEquals( 0, cache.getCache().size() );
	}

	@Test
	public void add() throws CacheExhaustedException {
		cache.add( KEY, RESOURCE );
		assertEquals( RESOURCE, cache.get( KEY ) );
		assertEquals( 1, cache.getCache().size() );
		assertEquals( "Expected two hits for add() and get()", 2, cache.getCache().get( KEY ).getCount() );
	}

	@Test
	public void addSizeLimit() throws CacheExhaustedException {
		// Define a cache limited to size one
		cache.setPruningStrategy( PruneStrategy.LEAST_USED );
		cache.setLimit( new SizeCacheLimit( 1 ) );

		// Add two resources
		cache.add( KEY, RESOURCE );
		cache.add( "another", "another" );

		// Check first item was pruned
		assertEquals( 1, cache.getCache().size() );
		assertEquals( null, cache.get( KEY ) );
		assertEquals( "another", cache.get( "another" ) );
	}

	@Test
	public void addCacheExhausted() throws CacheExhaustedException {
		exception.expect( CacheExhaustedException.class );
		exception.expectMessage( "Cache exhausted" );
		cache.setPruningStrategy( null );
		cache.setLimit( new SizeCacheLimit( 1 ) );
		cache.add( KEY, RESOURCE );
		cache.add( "another", "another" );
	}

	@Test
	public void clear() throws CacheExhaustedException {
		cache.add( KEY, RESOURCE );
		cache.clear();
		assertEquals( 0, cache.getCache().size() );
		assertEquals( null, cache.get( KEY ) );
	}
}
