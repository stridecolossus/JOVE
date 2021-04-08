package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

/**
 * Bounding volume defined by an axis-aligned box.
 * @author Sarge
 */
public final class BoundingBox implements BoundingVolume {
	private final Point min, max;

	/**
	 * Constructor.
	 * @param min Minimum corner
	 * @param max Maximum corner
	 */
	public BoundingBox(Point min, Point max) {
		this.min = notNull(min);
		this.max = notNull(max);
	}

	/**
	 * Constructor given extents.
	 * @param extents Extents
	 */
	public BoundingBox(Extents extents) {
		this(extents.min(), extents.max());
	}

	@Override
	public Extents extents() {
		return new Extents(min, max);
	}

	@Override
	public boolean contains(Point pt) {
		if(!contains(pt.x(), min.x(), max.x())) return false;
		if(!contains(pt.y(), min.y(), max.y())) return false;
		if(!contains(pt.z(), min.z(), max.z())) return false;
		return true;
	}

	private static boolean contains(float value, float min, float max) {
		return (value >= min) && (value <= max);
	}

	@Override
	public Optional<Point> intersect(Ray ray) {
		// TODO
		return null;
	}

	@Override
	public boolean intersects(BoundingVolume vol) {
		return vol.intersects(extents());
	}

	@Override
	public boolean intersects(Point centre, float radius) {
		// TODO
		return false;
	}

	@Override
	public boolean intersects(Extents extents) {
		// TODO
		return false;
	}
}
