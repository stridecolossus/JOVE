package org.sarge.jove.scene;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;

/**
 * Camera view frustum.
 * @author Sarge
 */
class Frustum implements BoundingVolume {
	private final Plane[] planes;

	/**
	 * Constructor.
	 * @param planes Frustum planes
	 */
	Frustum(Plane[] planes) {
		this.planes = Arrays.copyOf(planes, planes.length);
	}

	/**
	 * @return Frustum planes
	 */
	public Plane[] planes() {
		return Arrays.copyOf(planes, planes.length);
	}

	@Override
	public boolean contains(Point pt) {
		for(int n = 0; n < planes.length; ++n) {
			if(planes[n].side(pt) == Plane.Side.BACK) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Extents extents() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Point> intersect(Ray ray) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean intersects(BoundingVolume vol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean intersects(Point centre, float radius) {
		for(int n = 0; n < planes.length; ++n) {
			if(Math.abs(planes[n].distance(centre)) > radius) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean intersects(Extents extents) {
		// TODO - test min/max vs each plane
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
