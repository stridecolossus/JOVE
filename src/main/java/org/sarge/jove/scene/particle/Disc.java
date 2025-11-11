package org.sarge.jove.scene.particle;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

/**
 * A <i>disc</i> is a helper class for generating points and vectors on a disc.
 * <p>
 * Results are <b>not</b> evenly distributed.
 * <p>
 * @see <a href="https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone">Random cone vector</a>
 * @author Sarge
 */
class Disc {
	private final Normal normal;
	private final Vector x, y;
//	private final Interpolator radius;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param normal			Disc normal
	 * @param radius			Radius
	 * @param randomiser		Randomiser
	 */
	public Disc(Normal normal, float radius, Randomiser randomiser) {
		this.normal = requireNonNull(normal);
		this.x = this.normal.cross(Axis.minimal(normal));
		this.y = x.cross(this.normal);
//		this.radius = Interpolator.linear(-radius, +radius);
		this.randomiser = requireNonNull(randomiser);
	}

	/**
	 * Generates a random point on this disc.
	 * @return Random point
	 */
	public Point point() {
		final Vector dx = random(x);
		final Vector dy = random(y);
		return new Point(dx).add(dy);
	}

	/**
	 * Generates a random vector where this disc represents a cone.
	 * @return Random vector
	 */
	public Normal vector() {
		final Vector vec = new Vector(point());
		return new Normal(normal.add(vec));
	}
	// TODO - normal?

	/**
	 * Generates a random vector on the given local axis.
	 * @param axis Local axis
	 * @return Random vector
	 */
	private Vector random(Vector axis) {
		// TODO
		//final float r = radius.apply(randomiser.next());
		final float r = randomiser.next();
		return axis.multiply(r);
	}
	// TODO - normal?
}
