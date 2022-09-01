package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;
import org.sarge.jove.util.FloatSupplier;

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
 * @author Sarge
 */
public class ConeVectorFactory implements VectorFactory {
	private final Vector normal;
	private final Vector x, y;
	private final float radius;
	private final FloatSupplier random;

	/**
	 * Constructor.
	 * @param normal	Cone normal
	 * @param radius	Radius
	 * @param random	Randomiser in the range 0..1
	 */
	public ConeVectorFactory(Vector normal, float radius, FloatSupplier random) {
		this.normal = notNull(normal);
		this.x = right(normal);
		this.y = x.cross(normal);
		this.radius = radius;
		this.random = notNull(random);
	}

	/**
	 * Determines an arbitrary vector that is orthogonal to the cone normal.
	 * @see <a href="https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone">Random cone vector</a>
	 */
	private static Vector right(Vector normal) {
		final int index = min(normal);
		final Vector[] axes = {Vector.X, Vector.Y, Vector.Z};
		return normal.cross(axes[index]);
	}

	/**
	 * @return Index of the minimal component of the normal
	 */
	private static int min(Vector normal) {
		if(normal.x < normal.y) {
			return normal.x < normal.z ? 0 : 2;
		}
		else {
			return normal.y < normal.z ? 1 : 2;
		}
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
		final float angle = (1 - random.get()) * radius - radius;
		final var rot = new AxisAngle(axis, angle);
		return rot.rotate(normal);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(normal).append("radius", radius).build();
	}
}
