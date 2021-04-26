package org.sarge.jove.scene;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;

/**
 * A <i>compound volume</i> is comprised of a list of bounding volumes.
 * <p>
 * Note that this implementation does not make any assumptions or apply any constraints on the relationship between the list of volumes (other than their order).
 * For example it is possible to create an illogical but valid compound volume where the bounding volumes do not overlap.
 * <p>
 * Notes:
 * <ul>
 * <li>The {@link #contains(Point)} and {@link #intersects(Volume)} methods pass if <b>all</b> volumes pass</li>
 * <li>{@link #extents()} assumes at least one volume is present</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class CompoundVolume implements Volume {
	/**
	 * Creates a compound volume.
	 * @param volumes Volumes
	 * @return New compound volume
	 */
	public static Volume of(Volume... volumes) {
		return new CompoundVolume(Arrays.asList(volumes));
	}

	private final List<Volume> volumes;

	/**
	 * Constructor.
	 * @param volumes Volumes
	 */
	public CompoundVolume(List<Volume> volumes) {
		this.volumes = List.copyOf(volumes);
	}

	/**
	 * @throws IndexOutOfBoundsException if this compound volume is empty
	 */
	@Override
	public Extents extents() {
		return volumes.get(0).extents();
	}

	@Override
	public boolean contains(Point pt) {
		return volumes.stream().allMatch(v -> v.contains(pt));
	}

	@Override
	public Optional<Point> intersect(Ray ray) {
		// TODO
		return null;
	}

	@Override
	public boolean intersects(Volume vol) {
		return volumes.stream().allMatch(v -> v.intersects(vol));
	}

	@Override
	public int hashCode() {
		return volumes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof CompoundVolume that) && this.volumes.equals(that.volumes);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(volumes).build();
	}
}
