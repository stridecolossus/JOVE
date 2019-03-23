package org.sarge.jove.geometry;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Bounding volume.
 * @author Sarge
 */
public interface BoundingVolume {
	/**
	 * @param pt Point
	 * @return Whether this volume contains the given point
	 */
	boolean contains(Point pt);

	/**
	 * Determines the intersection of this volume and given ray
	 * @param ray Ray
	 * @return Intersection
	 * TODO - introduce lazy evaluated intersection point, i.e. this just returns y/n with method to then determine actual intersection point
	 */
	Optional<Point> intersect(Ray ray);

	/**
	 * @return Extents of this volume
	 */
	Extents extents();

	/**
	 * Determines whether this volume intersects the given volume.
	 * @param vol Volume
	 * @return Whether the volumes intersects
	 */
	boolean intersects(BoundingVolume vol);

	/**
	 * Determines whether this volume intersects the given volume.
	 * @param vol Volume
	 * @return Whether the volumes intersects
	 */
	boolean intersects(Point centre, float radius);

	/**
	 * Determines whether this volume intersects the given volume.
	 * @param vol Volume
	 * @return Whether the volumes intersects
	 */
	boolean intersects(Extents extents);

	/**
	 * Empty bounding volume.
	 */
	BoundingVolume EMPTY = new BoundingVolume() {
		@Override
		public boolean contains(Point pt) {
			return false;
		}

		@Override
		public Optional<Point> intersect(Ray ray) {
			return Optional.empty();
		}

		@Override
		public Extents extents() {
			return Extents.EMPTY;
		}

		@Override
		public boolean intersects(BoundingVolume vol) {
			return true;
		}

		@Override
		public boolean intersects(Point centre, float radius) {
			return true;
		}

		@Override
		public boolean intersects(Extents extents) {
			return true;
		}
	};

	/**
	 * Compound or recursive bounding volume.
	 * TODO - no test that each is russian doll
	 * @param volumes Compound volumes
	 * @return Compound volume
	 */
	static BoundingVolume compound(List<BoundingVolume> volumes) {
		if(volumes.isEmpty()) throw new IllegalArgumentException("Compound volume cannot be empty");
		return new BoundingVolume() {
			@Override
			public Extents extents() {
				return volumes.get(0).extents();
			}

			@Override
			public boolean contains(Point pt) {
				return volumes.stream().allMatch(bv -> bv.contains(pt));
			}

			@Override
			public boolean intersects(Extents extents) {
				return volumes.stream().allMatch(bv -> bv.intersects(extents));
			}

			@Override
			public boolean intersects(Point centre, float radius) {
				return volumes.stream().allMatch(bv -> bv.intersects(centre, radius));
			}

			@Override
			public boolean intersects(BoundingVolume vol) {
				// TODO - or recurse
				return vol.intersects(extents());
			}

			@Override
			public Optional<Point> intersect(Ray ray) {
				// TODO
				return null;
			}
		};
	}

	/**
	 * Creates an inverted bounding volume, i.e. everything outside of the given volume.
	 * @param vol Volume
	 * @return Inverse volume
	 */
	static BoundingVolume inverse(BoundingVolume vol) {
		return new BoundingVolume() {
			@Override
			public boolean contains(Point pt) {
				return !vol.contains(pt);
			}

			@Override
			public Optional<Point> intersect(Ray ray) {
				// TODO
				//return !intersects(ray);
				return null;
			}

			@Override
			public Extents extents() {
				// TODO
				return vol.extents();
			}

			@Override
			public boolean intersects(BoundingVolume vol) {
				return !vol.intersects(vol);
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

			@Override
			public String toString() {
				return ToStringBuilder.reflectionToString(this);
			}
		};
	}
}
