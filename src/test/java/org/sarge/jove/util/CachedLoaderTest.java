package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CachedLoaderTest {
	private static final String PATH = "path";
	private static final String RESOURCE = "resource";

	private CachedLoader<String> cachedLoader;
	private Loader<String> loader;
	private Cache<String> cache;

	@Rule public ExpectedException exception = ExpectedException.none();

	@Before
	public void before() throws IOException {
		loader = mock( Loader.class );
		cache = new Cache<>();
		cachedLoader = new CachedLoader<>( loader, cache );
		when( loader.load( PATH ) ).thenReturn( RESOURCE );
	}

	@Test
	public void load() throws IOException {
		// Load entry and check underlying loader invoked
		final String result = cachedLoader.load( PATH );
		assertEquals( RESOURCE, result );
		verify( loader ).load( PATH );

		// Load again and check cached value returned
		assertEquals( result, cachedLoader.load( PATH ) );
		verifyNoMoreInteractions( loader );
	}

	@Test
	public void loadSizeLimit() throws IOException {
		// Define a cache limited to size one
		cache.setPruningStrategy( PruneStrategy.LEAST_USED );
		cache.setLimit( new SizeCacheLimit( 1 ) );

		// Load two items and
		cachedLoader.load( PATH );
		cachedLoader.load( "another" );

		// Check first item was pruned
		assertEquals( 1, cache.getCache().size() );
		assertEquals( null, cache.get( PATH ) );
		assertEquals( true, cache.getCache().containsKey( "another" ) );
	}

	@Test
	public void loadCacheExhausted() throws IOException {
		exception.expect( IOException.class );
		exception.expectMessage( "Cache exhausted" );
		cache.setPruningStrategy( null );
		cache.setLimit( new SizeCacheLimit( 1 ) );
		cachedLoader.load( PATH );
		cachedLoader.load( "another" );
	}
}
