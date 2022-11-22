package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.scene.volume.*;

/**
 *
 * @author Sarge
 */
public class AggregateVolume implements Volume {
//	private final Function<Bounds, Volume> ctor;
	private final Class<? extends Volume> clazz;
	private transient Volume vol = EmptyVolume.INSTANCE;

	/**
	 * Constructor.
	 * @param clazz Aggregated volume type
	 * @throws IllegalArgumentException if {@link #clazz} is an aggregate class
	 */
	public AggregateVolume(Class<? extends Volume> clazz) {
		if(AggregateVolume.class.isAssignableFrom(clazz)) throw new IllegalArgumentException();
		this.clazz = notNull(clazz);
	}

	@Override
	public Bounds bounds() {
		return vol.bounds();
	}

//	/**
//	 * Calculates this volume as the aggregate of the given contained volumes.
//	 * @param volumes Contained volumes
//	 */
//	public void aggregate(Collection<Volume> volumes) {
//		// TODO
//		// - convert to bounds
//		// - determine min/max
//		// - create volume
//		// Q - how to get constructor???
//	}

	@Override
	public Intersection intersection(Ray ray) {
		return vol.intersection(ray);
	}

	@Override
	public boolean contains(Point pt) {
		return vol.contains(pt);
	}

	@Override
	public boolean intersects(Plane plane) {
		return vol.intersects(plane);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(clazz).build();
	}
}
