package org.sarge.jove.util;

/**
 * Pool of objects.
 * @author Sarge
 * @param <T> Object type
 */
public interface ObjectPool<T> {
	/**
	 * Thrown when the pool has been exhausted and no further objects can be created.
	 */
	class PoolExhaustedException extends RuntimeException {
		public PoolExhaustedException( String reason ) {
			super( reason );
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
	 * @throws PoolExhaustedException if the pool is empty
	 */
	T get() throws PoolExhaustedException;

	/**
	 * Releases the given object back to the pool.
	 * @param obj Object to add to the pool
	 */
	void restore( T obj );
}
