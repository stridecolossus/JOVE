package org.sarge.jove.scene.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>reflection collision</i> reflects a collided particle about the surface normal and optionally absorbs some of the velocity.
 * @see Particle#reflect(Point, Vector)
 * @author Sarge
 */
public class ReflectionCollision implements Collision {
	private final float absorb;

	/**
	 * Constructor.
	 * @param absorb Absorption factor
	 */
	public ReflectionCollision(float absorb) {
		if((absorb <= 0) || (absorb > 1)) throw new IllegalArgumentException("Arborption factor must be non-zero and less-than one");
		this.absorb = absorb;
	}

	/**
	 * Default constructor that does not apply absorption.
	 */
	public ReflectionCollision() {
		this(1);
	}

	@Override
	public void collide(Particle p, Intersection intersection) {
		final Point pos = intersection.point();
		final Normal normal = intersection.normal();
		p.reflect(pos, normal);
		p.velocity(absorb);
	}
}
