package org.sarge.jove.scene.particle;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

/**
 * The <i>sphere position factory</i> generates particles on the surface of a sphere.
 * @author Sarge
 */
public class SpherePositionFactory implements PositionFactory {
	private final Point centre;
	private final float radius;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param sphere			Sphere
	 * @param randomiser		Randomiser
	 */
	public SpherePositionFactory(Point centre, float radius, Randomiser randomiser) {
		this.centre = requireNonNull(centre);
		this.radius = requireZeroOrMore(radius);
		this.randomiser = requireNonNull(randomiser);
	}

	@Override
	public Point position() {
		final Vector vec = randomiser.vector().normalize().multiply(radius);
		return centre.add(vec);
	}
}
