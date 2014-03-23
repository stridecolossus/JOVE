package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.Cache.CacheExhaustedException;
import org.sarge.jove.util.Cache.CacheLimit;

public class SizeCacheLimitTest {
	private CacheLimit limit;

	@Before
	public void before() {
		limit = new SizeCacheLimit( 1 );
	}

	@Test
	public void isFull() throws CacheExhaustedException {
		final Cache<String> cache = new Cache<>();
		assertEquals( false, limit.isFull( cache ) );
		cache.add( "key", "resource" );
		assertEquals( true, limit.isFull( cache ) );
	}
}
