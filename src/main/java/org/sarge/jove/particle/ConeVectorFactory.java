package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

/**
 * A <i>cone vector factory</i> generates randomised vectors within a cone.
 * <p>
 * Notes:
 * <ul>
 * <li>Results can be assumed to <b>not</b> be evenly distributed</li>
 * <li>The algorithm selects <b>two</b> random values to generate the resultant vector</li>
 * <li>The default implementation performs rotation using an {@link AxisAngle} in the {@link #rotate(Vector)} override method</li>
 * </ul>
 * <p>
 * @see <a href="https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone">Random cone vector</a>
 * @author Sarge
 */
public class ConeVectorFactory implements VectorFactory {
	private final Vector normal;
	private final Vector x, y;
	private final Interpolator radius;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param normal	Cone normal
	 * @param radius	Radius
	 * @param random	Randomiser
	 */
	public ConeVectorFactory(Vector normal, float radius, Randomiser random) {
		this.normal = notNull(normal);
		this.x = normal.cross(Axis.minimal(normal));
		this.y = x.cross(normal);
		this.radius = Interpolator.linear(-radius, +radius);
		this.randomiser = notNull(random);
	}

	@Override
	public Vector vector(Point pos) {
		final Vector dx = rotate(x);
		final Vector dy = rotate(y);
		return dx.add(dy).normalize();
	}

	/**
	 * Rotates the cone normal about a randomly generated component vector.
	 * @param axis Rotation axis
	 * @return Normal rotated about the given vector
	 */
	protected Vector rotate(Vector axis) {
		final float angle = radius.apply(randomiser.next());
		final var rot = AxisAngle.of(axis, angle);
		return rot.rotate(normal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("normal", normal)
				.append("radius", radius)
				.build();
	}

	/**
	 * Loads a cone vector factory from the given element.
	 * @param e					Element
	 * @param randomiser		Randomiser
	 * @return Cone vector factory
	 */
	public static VectorFactory load(Element e, Randomiser randomiser) {
		final Vector normal = e.child("normal").text().transform(Axis.CONVERTER);
		final float radius = e.child("radius").text().toFloat();
		return new ConeVectorFactory(normal, radius, randomiser);
	}
}
