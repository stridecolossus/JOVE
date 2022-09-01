package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.util.Random;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;

/**
 * A <i>cone vector factory</i> generates randomised vectors specified by a cone.
 * @author Sarge
 */
public class ConeVectorFactory implements VectorFactory {
	private final Vector normal, vec;
	private final float radius;
	private final Random random;

	/**
	 * Constructor.
	 * @param normal	Cone normal
	 * @param radius	Radius
	 * @param random	Randomiser
	 */
	public ConeVectorFactory(Vector normal, float radius, Random random) {
		this.normal = notNull(normal);
		this.vec = cross(normal);
		this.radius = radius;
		this.random = notNull(random);
	}

	/**
	 * Determines an arbitrary vector that is orthogonal to the cone normal.
	 */
	private static Vector cross(Vector normal) {
		// Determine index of minimum component of the normal
		// TODO - this is very crude, walks vec twice
		int index = 0;
		float min = Float.MAX_VALUE;
		for(int n = 0; n < 3; ++n) {
			final float c = normal.get(n);
			if(c < min) {
				min = c;
				index = n;
			}
		}

		// Create cardinal axis
		final float[] vec = new float[3];
		vec[index] = 1;
		final Vector axis = new Vector(vec);

		// Calc orthogonal vector
		return normal.cross(axis).normalize();
	}

	@Override
	public Vector vector(Point pos) {
		final float angle = random.nextFloat(radius);
		final var rot = new AxisAngle(normal, angle);
		return rot.rotate(vec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(normal).append("radius", radius).build();
	}
}

// https://stackoverflow.com/questions/19337314/generate-random-point-on-a-2d-disk-in-3d-space-given-normal-vector
// https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone

// https://mathworld.wolfram.com/DiskPointPicking.html
