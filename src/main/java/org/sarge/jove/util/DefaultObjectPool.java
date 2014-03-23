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
	 * Factory method for an object-pool using the default constructor of the given class.
	 * @param clazz Object pool type
	 * @return Object pool
	 */
	public static <T> DefaultObjectPool<T> create( final Class<T> clazz ) {
		Check.notNull( clazz );
		return new DefaultObjectPool<T>() {
			@Override
			protected T create() {
				try {
					return clazz.newInstance();
				}
				catch( Exception e ) {
					throw new RuntimeException( "Error creating new pool object: " + clazz.getName(), e );
				}
			}
		};
	}

	private final Queue<T> pool = new ArrayDeque<>();

	private int max = Integer.MAX_VALUE;
	private GrowthPolicy growth = new IncrementGrowthPolicy( 1 );

	private int created;

	/**
	 * Default constructor.
	 * <p>
	 * The pool:
	 * <ul>
	 * <li>is initially empty</li>
	 * <li>can grow on demand</li>
	 * <li>has an unlimited maximum size</li>
	 * </ul>
	 */
	public DefaultObjectPool() {
	}

	@Override
	public int getNumberCreated() {
		return created;
	}

	@Override
	public int getSize() {
		return pool.size();
	}

	/**
	 * Sets the maximum pool size.
	 * @param max Maximum pool size
	 */
	public void setMaximumSize( int max ) {
		Check.oneOrMore( max );
		if( created > max ) throw new IllegalArgumentException( "Pool size already exceeds specified max" );
		this.max = max;
	}

	/**
	 * Sets the growth policy of this pool.
	 * @param growth Growth policy or <tt>null</tt> for fixed pool
	 */
	public void setGrowthPolicy( GrowthPolicy growth ) {
		this.growth = growth;
	}

	@Override
	public T get() throws PoolException {
		if( pool.isEmpty() ) {
			// Check if more objects can be created
			if( created >= max ) throw new PoolException( "Maximum pool size reached" );
			if( growth == null ) throw new PoolException( "Cannot grow pool" );

			// Calc amount to grow
			int inc = growth.getSizeIncrement( created );
			if( inc < 1 ) throw new IllegalArgumentException( "Growth increment must be one-or-more" );

			// Grow the pool
			inc = Math.min( inc, max - created );
			create( inc );
		}

		// Get next free object from the pool
		return pool.poll();
	}

	@Override
	public void restore( T obj ) throws PoolException {
		Check.notNull( obj );
		if( pool.size() >= max ) throw new PoolException( "Maximum pool size exceeded" );
		pool.add( obj );
	}

	/**
	 * Adds a number of new objects to the pool.
	 * @param num Number to create
	 */
	public void create( int num ) throws PoolException {
		Check.oneOrMore( num );
		if( pool.size() + num >= max ) throw new PoolException( "Maximum pool size exceeded" );

		for( int n = 0; n < num; ++n ) {
			final T obj = create();
			pool.add( obj );
		}

		created += num;
	}

	/**
	 * Factory for new pool objects.
	 * Over-ride to create a pool that can grow on-demand.
	 * @return New object
	 * @throws PoolExhaustedException by default
	 */
	protected T create() {
		throw new PoolException( "No create method implemented" );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
