package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.DefaultObjectPool.ExpandGrowthPolicy;
import org.sarge.jove.util.DefaultObjectPool.GrowthPolicy;
import org.sarge.jove.util.DefaultObjectPool.IncrementGrowthPolicy;
import org.sarge.jove.util.ObjectPool.PoolExhaustedException;

public class DefaultObjectPoolTest {
	private DefaultObjectPool<Object> pool;
	private int count;

	@Before
	public void before() {
		pool = null;
		count = 0;
	}

	private void init( int size, Integer max, GrowthPolicy growth ) {
		pool = new DefaultObjectPool<Object>( size, max, growth ) {
			@Override
			protected Object create() {
				++count;
				return new Object();
			}
		};
	}

	@Test
	public void get() {
		init( 0, null, new IncrementGrowthPolicy( 1 ) );
		final Object result = pool.get();
		assertNotNull( result );
		assertEquals( 1, count );
		assertEquals( 1, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );
	}

	@Test
	public void restore() {
		init( 0, null, null );
		final Object obj = new Object();
		pool.restore( obj );
		assertEquals( obj, pool.get() );
	}

	@Test
	public void create() {
		init( 0, null, null );
		pool.create( 3 );
		assertEquals( 3, count );
		assertEquals( 3, pool.getNumberCreated() );
		assertEquals( 3, pool.getSize() );
	}

	@Test
	public void incrementGrowth() {
		init( 0, null, new IncrementGrowthPolicy( 3 ) );
		pool.get();
		assertEquals( 3, count );
		assertEquals( 3, pool.getNumberCreated() );
		assertEquals( 2, pool.getSize() );
	}

	@Test
	public void expandGrowth() {
		// Create an initially pool with a 100% growth rate
		init( 0, null, new ExpandGrowthPolicy( 1 ) );

		// Get from empty pool and check at least one object created
		pool.get();
		assertEquals( 1, count );
		assertEquals( "Expected at least one object to be created", 1, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );

		// Get again from empty pool and check expand rate applied
		pool.get();
		assertEquals( 2, count );
		assertEquals( 2, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );

		// Repeat and check doubled again
		pool.get();
		assertEquals( 4, count );
		assertEquals( 4, pool.getNumberCreated() );
		assertEquals( 1, pool.getSize() );
	}

	@Test
	public void checkGrowthLimited() {
		init( 0, 2, new IncrementGrowthPolicy( 3 ) );
		pool.get();
		assertEquals( 2, pool.getNumberCreated() );
		assertEquals( 2, count );
	}

	@Test(expected=PoolExhaustedException.class)
	public void maximumSizeExceeded() {
		init( 1, 1, new IncrementGrowthPolicy( 1 ) );
		pool.get();
		pool.get();
	}

	@Test(expected=PoolExhaustedException.class)
	public void growthPolicyNotSpecified() {
		init( 0, null, null );
		pool.get();
	}

	@SuppressWarnings("unused")
	@Test(expected=PoolExhaustedException.class)
	public void defaultFactoryMethod() {
		new DefaultObjectPool<>( 1, 1, null );
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void initialSizeExceedsMaximum() {
		new DefaultObjectPool<>( 2, 1, null );
	}
}
