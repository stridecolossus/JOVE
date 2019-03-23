package org.sarge.jove.particle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Position factory that generates particles on a spherical surface.
 * @author Sarge
 */
public class SphericalPositionFactory implements PositionFactory {
	private final float radius;

	/**
	 * Constructor.
	 * @param radius Sphere radius
	 */
	public SphericalPositionFactory(float radius) {
		this.radius = radius;
	}

	@Override
	public Point position() {
		return new Point(Vector.random().scale(radius));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
