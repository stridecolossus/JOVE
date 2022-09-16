package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

/**
 * A <i>disc</i> is a helper class for generating points and vectors on a disc.
 * <p>
 * Results can be assumed to <b>not</b> be evenly distributed.
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
		this.x = this.normal.cross(Axis.minimal(normal));
		this.y = x.cross(this.normal);
		this.radius = Interpolator.linear(-radius, +radius);
		this.randomiser = notNull(randomiser);
	}

	/**
	 * Generates a random point on this disc.
	 * @return Random point
	 */
	public Point point() {
		final Point dx = new Point(random(x));
		final Tuple dy = random(y);
		return dx.add(dy);
	}

	/**
	 * Generates a random vector in the given direction.
	 * @param dir Direction
	 * @return Random vector
	 */
	private Tuple random(Vector dir) {
		final float r = radius.apply(randomiser.next());
		return dir.multiply(r);
	}

	/**
	 * Generates a randomised vector where this disc defines a cone.
	 * @return Cone vector
	 */
	public Vector vector() {
		final Vector dx = rotate(x);
		final Vector dy = rotate(y);
		return dx.add(dy).normalize();
	}

	/**
	 * Rotates the cone normal about a randomly generated component vector.
	 * Note that the <i>radius</i> is assumed as an <b>angle</b> about the normal (radians).
	 * @param axis Rotation axis
	 * @return Normal rotated about the given vector
	 */
	private Vector rotate(Vector axis) {
		final float angle = radius.apply(randomiser.next());
		final var rot = AxisAngle.of(axis, angle);
		return rot.rotate(normal);
	}

	/**
	 * Loads a disc from the given element.
	 * @param e					Element
	 * @param randomiser		Randomiser
	 * @return Disc
	 */
	public static Disc load(Element e, Randomiser randomiser) {
		final Vector normal = e.child("normal").text().transform(Axis.CONVERTER);
		final float radius = e.child("radius").text().toFloat();
		return new Disc(normal, radius, randomiser);
	}
}
