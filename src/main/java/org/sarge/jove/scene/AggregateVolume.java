package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 *
 * @author Sarge
 */
public class AggregateVolume implements Volume {
//	private final Function<Bounds, Volume> ctor;
	private final Class<? extends Volume> clazz;
	private transient Volume vol = Volume.EMPTY;

	/**
	 * Constructor.
	 * @param clazz Aggregated volume type
	 * @throws IllegalArgumentException if {@link #clazz} is an aggregate class
	 */
	public AggregateVolume(Class<? extends Volume> clazz) {
		if(AggregateVolume.class.isAssignableFrom(clazz)) throw new IllegalArgumentException();
		this.clazz = notNull(clazz);
	}

	/**
	 * Calculates this volume as the aggregate of the given contained volumes.
	 * @param volumes Contained volumes
	 */
	public void aggregate(Collection<Volume> volumes) {
		// TODO
		// - convert to bounds
		// - determine min/max
		// - create volume
		// Q - how to get constructor???
	}

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
