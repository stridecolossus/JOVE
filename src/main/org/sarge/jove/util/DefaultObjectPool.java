package org.sarge.jove.util;

import java.util.ArrayDeque;
import java.util.Queue;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Default implementation.
 * <p>
 * Over-ride the {@link DefaultObjectPool#create()} factory method to create a pool that can grow on-demand.
 * <p>
 * @author Sarge
 * @param <T> Object type
 */
public class DefaultObjectPool<T> implements ObjectPool<T> {
	/**
	 * Defines the growth policy of the pool.
	 */
	public static interface GrowthPolicy {
		/**
		 * Calculates how much the pool should grow by.
		 * @param size Current pool size
		 * @return Size increment
		 */
		int getSizeIncrement( int size );
	}

	/**
	 * Grows the pool by a fixed increment.
	 */
	public static class IncrementGrowthPolicy implements GrowthPolicy {
		private final int amount;

		/**
		 * Constructor.
		 * @param amount Increment amount
		 */
		public IncrementGrowthPolicy( int amount ) {
			Check.oneOrMore( amount );
			this.amount = amount;
		}

		@Override
		public int getSizeIncrement( int size ) {
			return amount;
		}

		@Override
		public String toString() {
			return ToString.toString( this );
		}
	}

	/**
	 * Grows the pool as a percentage of the current pool size.
	 */
	public static class ExpandGrowthPolicy implements GrowthPolicy {
		private final float rate;

		/**
		 * Constructor.
		 * @param rate Growth rate
		 */
		public ExpandGrowthPolicy( float rate ) {
			if( rate <= 0 ) throw new IllegalArgumentException( "Growth rate must be positive" );
			this.rate = rate;
		}

		@Override
		public int getSizeIncrement( int size ) {
			return Math.max( 1, (int) ( size * rate ) );
		}

		@Override
		public String toString() {
			return ToString.toString( this );
		}
	}

	private final Queue<T> pool = new ArrayDeque<>();

	private final Integer max;
	private final GrowthPolicy growth;

	private int created;

	/**
	 * Constructor given an initial pool size.
	 * @param size		Initial pool size (zero-or-more)
	 * @param max		Maximum pool size or <tt>null</tt> if unlimited
	 * @param growth	Growth policy or <tt>null</tt> for fixed pool size
	 */
	public DefaultObjectPool( int size, Integer max, GrowthPolicy growth ) {
		Check.zeroOrMore( size );
		if( ( max != null ) && ( size > max ) ) throw new IllegalArgumentException( "Initial size exceeds maximum" );

		this.max = max;
		this.growth = growth;

		if( size > 0 ) {
			create( size );
		}
	}

	/**
	 * Default constructor.
	 * <p>
	 * The default pool:
	 * <ul>
	 * <li>is initially empty</li>
	 * <li>grows by one object on a call to {@link #get()}</li>
	 * <li>has no size limit</li>
	 * </ul>
	 */
	public DefaultObjectPool() {
		this( 0, null, new IncrementGrowthPolicy( 1 ) );
	}

	@Override
	public int getNumberCreated() {
		return created;
	}

	@Override
	public int getSize() {
		return pool.size();
	}

	@Override
	public T get() throws PoolExhaustedException {
		if( pool.isEmpty() ) {
			// Check if more objects can be created
			if( ( max != null ) && ( created >= max ) ) throw new PoolExhaustedException( "Maximum pool size reached" );
			if( growth == null ) throw new PoolExhaustedException( "Cannot grow pool" );

			// Calc amount to grow
			int inc = growth.getSizeIncrement( created );
			if( inc < 1 ) throw new IllegalArgumentException( "Growth increment must be one-or-more" );

			// Grow the pool
			if( max != null ) {
				inc = Math.min( inc, max - created );
			}
			create( inc );
		}

		// Get next free object from the pool
		return pool.poll();
	}

	@Override
	public void restore( T obj ) {
		Check.notNull( obj );
		pool.add( obj );
	}

	/**
	 * Adds a number of new objects to the pool.
	 * @param num Number to create
	 */
	protected void create( int num ) {
		Check.oneOrMore( num );

		for( int n = 0; n < num; ++n ) {
			final T obj = create();
			pool.add( obj );
		}

		created += num;
	}

	/**
	 * New object factory.
	 * @return New object
	 * @throws PoolExhaustedException by default
	 */
	protected T create() {
		throw new PoolExhaustedException( "No object factory" );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
