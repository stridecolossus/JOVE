package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sarge.jove.util.ObjectPool.ExpandGrowthPolicy;
import org.sarge.jove.util.ObjectPool.IncrementGrowthPolicy;
import org.sarge.jove.util.ObjectPool.PoolException;

public class DefaultObjectPoolTest {
	private DefaultObjectPool<Object> pool;

	@Rule public ExpectedException exception = ExpectedException.none();

	@Before
	public void before() {
		pool = DefaultObjectPool.create( Object.class );
	}

	@Test
	public void get() {
		final Object result = pool.get();
		assertNotNull( result );
		assertEquals( 1, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );
	}

	@Test
	public void restore() {
		// Get and restore an object to the pool
		final Object obj = pool.get();
		pool.restore( obj );
		assertEquals( 1, pool.getNumberCreated() );
		assertEquals( 1, pool.getSize() );

		// Check same object is restored
		assertEquals( obj, pool.get() );
		assertEquals( 0, pool.getSize() );
	}

	@Test(expected=PoolException.class)
	public void restoreNotMember() {
		pool.restore( new Object() );
	}

	@Test
	public void incrementGrowth() {
		pool.setGrowthPolicy( new IncrementGrowthPolicy( 3 ) );
		pool.get();
		assertEquals( 3, pool.getNumberCreated() );
		assertEquals( 2, pool.getSize() );
	}

	@Test
	public void expandGrowth() {
		// Create an initially empty pool with a 100% growth rate
		pool.setGrowthPolicy( new ExpandGrowthPolicy( 1 ) );

		// Get from empty pool and check at least one object created
		pool.get();
		assertEquals( "Expected at least one object to be created", 1, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );

		// Get again from empty pool and check expand rate applied
		pool.get();
		assertEquals( 2, pool.getNumberCreated() );
		assertEquals( 0, pool.getSize() );

		// Repeat and check doubled again
		pool.get();
		assertEquals( 4, pool.getNumberCreated() );
		assertEquals( 1, pool.getSize() );
	}

	@Test
	public void checkGrowthLimited() {
		exception.expect( PoolException.class );
		exception.expectMessage( "Maximum pool size exceeded" );
		pool.setMaximumSize( 1 );
		pool.get();
		pool.get();
	}

	@Test
	public void maximumSizeExceeded() {
		exception.expect( PoolException.class );
		exception.expectMessage( "Maximum pool size exceeded" );
		pool.setMaximumSize( 1 );
		pool.get();
		pool.get();
	}

	@Test
	public void growthPolicyNotSpecified() {
		exception.expect( PoolException.class );
		exception.expectMessage( "Cannot grow pool" );
		pool.setGrowthPolicy( null );
		pool.get();
	}

	@Test
	public void createNotImplemented() {
		exception.expect( PoolException.class );
		exception.expectMessage( "No create method" );
		pool = new DefaultObjectPool<>();
		pool.get();
	}

	@Test
	public void create() {
		pool.create( 3 );
		assertEquals( 3, pool.getSize() );
		assertEquals( 3, pool.getNumberCreated() );
	}

	@Test
	public void createMaxSizeExceeded() {
		exception.expect( PoolException.class );
		exception.expectMessage( "Maximum pool size exceeded" );
		pool.setMaximumSize( 2 );
		pool.create( 3 );
		assertEquals( 3, pool.getSize() );
		assertEquals( 3, pool.getNumberCreated() );
	}
}
