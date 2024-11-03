package org.sarge.jove.scene.particle;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

/**
 * The <i>sphere position factory</i> generates particles on the surface of a sphere.
 * @author Sarge
 */
public class SpherePositionFactory implements PositionFactory {
	private final Sphere sphere;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param sphere			Sphere
	 * @param randomiser		Randomiser
	 */
	public SpherePositionFactory(Sphere sphere, Randomiser randomiser) {
		this.sphere = requireNonNull(sphere);
		this.randomiser = requireNonNull(randomiser);
	}

	@Override
	public Point position() {
		final Vector vec = randomiser.vector().normalize().multiply(sphere.radius());
		return sphere.centre().add(vec);
	}
}
