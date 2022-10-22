package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

/**
 * A <i>disc</i> is a helper class for generating points and vectors on a disc.
 * <p>
 * Results are <b>not</b> evenly distributed.
 * <p>
 * @see <a href="https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone">Random cone vector</a>
 * @author Sarge
 */
class Disc {
	private final Vector normal;
	private final Vector x, y;
	private final Interpolator radius;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param normal			Disc normal
	 * @param radius			Radius
	 * @param randomiser		Randomiser
	 */
	public Disc(Vector normal, float radius, Randomiser randomiser) {
		this.normal = normal.normalize();
		this.x = this.normal.cross(Axis.minimal(normal).vector());
		this.y = x.cross(this.normal);
		this.radius = Interpolator.linear(-radius, +radius);
		this.randomiser = notNull(randomiser);
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
	public Vector vector() {
		final Vector vec = new Vector(point());
		return normal.add(vec).normalize();
	}

	/**
	 * Generates a random vector on the given local axis.
	 * @param axis Local axis
	 * @return Random vector
	 */
	private Vector random(Vector axis) {
		final float r = radius.apply(randomiser.next());
		return axis.multiply(r);
	}

	/**
	 * Loads a disc from the given element.
	 * @param e					Element
	 * @param randomiser		Randomiser
	 * @return Disc
	 */
	public static Disc load(Element e, Randomiser randomiser) {
		final Vector normal = e.child("normal").text().transform(Axis::parse);
		final float radius = e.child("radius").text().toFloat();
		return new Disc(normal, radius, randomiser);
	}
}
