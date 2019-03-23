package org.sarge.jove.particle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Position factory based on an {@link Extents}.
 * @author Sarge
 */
public class ExtentsPositionFactory implements PositionFactory {
	private final Point min;
	private final Vector range;

	/**
	 * Constructor.
	 * @param extents Extents
	 */
	public ExtentsPositionFactory(Extents extents) {
		this.min = extents.min();
		this.range = Vector.of(extents.min(), extents.max());
	}

	@Override
	public Point position() {
		final Vector r = Vector.random();
		final Vector vec = new Vector(r.x * range.x, r.y * range.y, r.z * range.z);
		return min.add(vec);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
