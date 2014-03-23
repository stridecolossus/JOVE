package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.Cache.CacheEntry;

public class PruneStrategyTest {
	private CacheEntry<String> entry;

	@Before
	public void before() {
		entry = mock( CacheEntry.class );
	}

	@Test
	public void leastUsed() {
		when( entry.getCount() ).thenReturn( 42 );
		assertEquals( 42, PruneStrategy.LEAST_USED.evaluate( entry ) );
	}

	@Test
	public void oldest() {
		when( entry.getTime() ).thenReturn( 42L );
		assertEquals( 42, PruneStrategy.OLDEST.evaluate( entry ) );
	}
}
