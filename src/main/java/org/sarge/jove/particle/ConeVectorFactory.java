package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.util.Random;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Vector factory that generates a cone of particles.
 * @author Sarge
 */
public class ConeVectorFactory implements VectorFactory {
	private final Vector normal;
	private final float radius;
	private final Random random;

	/**
	 * Constructor.
	 * @param normal	Cone normal
	 * @param radius	Radius (radians)
	 * @throws IllegalArgumentException if the radius is larger than {@link MathsUtil#HALF_PI}
	 */
	public ConeVectorFactory(Vector normal, float radius, Random random) {
		Check.range(radius, 0f, MathsUtil.HALF_PI);
		this.normal = notNull(normal);
		this.radius = radius / 2f;
		this.random = notNull(random);
	}

	@Override
	public Vector vector(Point pos) {
		// TODO - is radius really in radians? does project actually do what we want?
		final float r = radius - random.nextFloat() * radius;
		final Vector vec = new Vector(0, 1, r);
		return vec.project(normal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("normal", normal).append("radius", radius).build();
	}
}
