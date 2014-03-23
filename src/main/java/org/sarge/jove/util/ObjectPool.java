package org.sarge.jove.util;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Pool of objects.
 * @author Sarge
 * @param <T> Object type
 */
public interface ObjectPool<T> {
	/**
	 * Thrown if the pool has become invalid.
	 */
	public class PoolException extends RuntimeException {
		public PoolException( String reason ) {
			super( reason );
		}
	}

	/**
	 * Defines the growth policy of this pool.
	 */
	interface GrowthPolicy {
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
	class IncrementGrowthPolicy implements GrowthPolicy {
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
	class ExpandGrowthPolicy implements GrowthPolicy {
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

	/**
	 * @return Total number of objects created
	 */
	int getNumberCreated();

	/**
	 * @return Number of available objects in the pool
	 */
	int getSize();

	/**
	 * @return Gets the next available object from the pool
	 * @throws PoolException if the pool is empty and no more objects can be generated
	 */
	T get() throws PoolException;

	/**
	 * Releases the given object back to the pool.
	 * @param obj Object to add to the pool
	 * @throws PoolException if the maximum pool size has been exceeded
	 */
	void restore( T obj ) throws PoolException;
}
