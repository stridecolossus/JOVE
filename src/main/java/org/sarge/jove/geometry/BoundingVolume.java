package org.sarge.jove.geometry;

/**
 * Defines a bounding volume for a model for culling and picking tests.
 * @author Sarge
 */
public interface BoundingVolume {
	/**
	 * @return Centre point of this volume
	 */
	Point getCentre();

	/**
	 * @param pt Point to test
	 * @return Whether this volume contains the given point
	 */
	boolean contains( Point pt );

	/**
	 * @param ray Picking ray
	 * @return Whether this volume is intersected by the given ray
	 */
	boolean intersects( Ray ray );
}
