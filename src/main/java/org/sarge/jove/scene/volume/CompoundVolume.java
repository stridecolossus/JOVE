package org.sarge.jove.scene.volume;

import java.util.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>compound volume</i> is comprised of a group of bounding volumes.
 * <p>
 * Note that this implementation does not make any assumptions or apply any constraints on the relationship between the list of volumes (other than their order).
 * <p>
 * This class can therefore model an irregular shape (e.g. a volume comprised of a cylinder and a cone to bound a rocket model)
 * or a <i>recursive</i> or <i>russian doll</i> volume (e.g. a simple sphere followed by a smaller but more accurate volume).
 * <p>
 * Intersection tests:
 * <ul>
 * <li>The {@link #contains(Point)} and {@link #intersects(Volume)} methods pass when <b>all</b> the volumes pass</li>
 * <li>{@link #intersect(Ray)} returns the <b>first</b> intersection found</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public record CompoundVolume(List<Volume> volumes) implements Volume {
	/**
	 * Creates a compound volume.
	 * @param volumes Volumes
	 * @return New compound volume
	 */
	public static Volume of(Volume... volumes) {
		return new CompoundVolume(Arrays.asList(volumes));
	}

	/**
	 * Constructor.
	 * @param volumes Volumes
	 */
	public CompoundVolume {
		volumes = List.copyOf(volumes);
	}

	@Override
	public Bounds bounds() {
		// TODO
		return null;
	}

	@Override
	public boolean contains(Point p) {
		return volumes.stream().allMatch(v -> v.contains(p));
	}

	@Override
	public boolean intersects(Volume vol) {
		return volumes.stream().allMatch(v -> v.intersects(vol));
	}

	@Override
	public boolean intersects(Plane plane) {
		return volumes.stream().allMatch(v -> v.intersects(plane));
	}

	@Override
	public Iterable<Intersection> intersections(Ray ray) {
//		// TODO
//		final Optional<Intersection> intersection = volumes
//				.stream()
//				.map(v -> v.intersect(ray))
//				.filter(Predicate.not(Intersection.NONE::equals))
//				.findAny();
		return null;
	}
}
