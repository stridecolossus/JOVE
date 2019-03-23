package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.range;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;
import org.sarge.jove.util.Randomiser;

/**
 * Vector factory that generates a cone of particles.
 * @author Sarge
 */
public class ConeVectorFactory implements VectorFactory {
	private final Vector normal;
	private final float radius;

	/**
	 * Constructor.
	 * @param normal	Cone normal
	 * @param radius	Radius (radians)
	 * @throws IllegalArgumentException if the radius is larger than {@link MathsUtil#HALF_PI}
	 */
	public ConeVectorFactory(Vector normal, float radius) {
		this.normal = notNull(normal);
		this.radius = range(radius, 0f, MathsUtil.HALF_PI) / 2f;
	}

	@Override
	public Vector vector(Point pos) {
		// TODO - is radius really in radians? does project actually do what we want?
		final float r = radius - Randomiser.RANDOM.nextFloat() * radius;
		final Vector vec = new Vector(0, 1, r);
		return vec.project(normal);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
