package org.sarge.jove.geometry;

import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Bounding volume comprised of multiple volumes that are tested in order.
 * @author Sarge
 */
public class CompoundVolume implements BoundingVolume {
	private final List<BoundingVolume> volumes;

	/**
	 * Constructor.
	 * @param volumes List of volumes
	 */
	public CompoundVolume(List<BoundingVolume> volumes) {
		Check.notEmpty(volumes);
		this.volumes = new ArrayList<>(volumes);
	}

	@Override
	public Point getCentre() {
		// TODO
		return volumes.get(0).getCentre();
	}

	@Override
	public boolean contains(Point pt) {
		return volumes.stream().anyMatch(vol -> vol.contains(pt));
	}

	@Override
	public boolean intersects(Ray ray) {
		return volumes.stream().anyMatch(vol -> vol.intersects(ray));
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
