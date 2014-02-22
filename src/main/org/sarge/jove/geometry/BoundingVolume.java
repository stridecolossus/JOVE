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
	 * Sets the centre point of this volume.
	 * @param centre Centre
	 */
	void setCentre( Point centre );

	/**
	 * Scales this volume.
	 * @param scale Scalar
	 */
	void scale( float scale );

	// TODO
	//boolean intersects( Frustum f );

	/**
	 * @param ray Picking ray
	 * @return Whether this volume is intersected by the given ray
	 */
	boolean intersects( Ray ray );
}
