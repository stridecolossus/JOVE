package org.sarge.jove.scene.particle;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.element.Element;

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
		this.sphere = notNull(sphere);
		this.randomiser = notNull(randomiser);
	}

	@Override
	public Point position() {
		final Vector vec = randomiser.vector().normalize().multiply(sphere.radius());
		return sphere.centre().add(vec);
	}

	/**
	 * Loads a sphere position factory from the given element.
	 * @param e					Element
	 * @param randomiser		Randomiser
	 * @return Sphere position factory
	 */
	public static SpherePositionFactory load(Element e, Randomiser randomiser) {
		final Point centre = e.child("centre").text().transform(Point.CONVERTER);
		final float radius = e.child("radius").text().transform(Float::parseFloat);
		final var sphere = new Sphere(centre, radius);
		return new SpherePositionFactory(sphere, randomiser);
	}
}
