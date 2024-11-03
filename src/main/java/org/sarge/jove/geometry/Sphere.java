package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>sphere</i> is defined by a radius about a centre-point.
 * @author Sarge
 */
public record Sphere(Point centre, float radius) {
	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public Sphere {
		requireNonNull(centre);
		requireZeroOrMore(radius);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Sphere that) &&
				MathsUtility.isApproxEqual(this.radius, that.radius) &&
				this.centre.equals(that.centre);
	}

	/**
	 * The <i>sphere normal factory</i> generates surface normals on the unit sphere.
	 */
	public static class NormalFactory {
		private final Cosine.Provider provider;

		/**
		 * Default constructor.
		 */
		public NormalFactory() {
			this(Cosine.Provider.DEFAULT);
		}

		/**
		 * Constructor.
		 * @param provider Cosine function
		 */
		public NormalFactory(Cosine.Provider provider) {
			this.provider = requireNonNull(provider);
		}

		/**
		 * Calculates the vector to the point on the unit-sphere for the given rotation angles (radians, counter-clockwise).
		 * <p>
		 * Note that by default a {@link #yaw} of zero is in the direction of the X axis.
		 * The sphere can be 'rotated' to point in the -Z direction by the {@link #rotate()} adapter method.
		 * <p>
		 * @param yaw		Horizontal angle in the range zero to {@link MathsUtility#TWO_PI}
		 * @param pitch		Vertical angle in the range +/- {@link MathsUtility#HALF_PI}
		 * @return Unit-sphere surface vector
		 */
		public Normal vector(float yaw, float pitch) {
			final Cosine theta = provider.cosine(yaw);
			final Cosine phi = provider.cosine(pitch);
			final float x = theta.cos() * phi.cos();
			final float y = phi.sin();
			final float z = theta.sin() * phi.cos();
			return new Normal(new Vector(x, y, z));
		}

		/**
		 * Adapts this factory by rotating the <i>yaw</i> angle to point in the -Z direction.
		 * @return Rotated vector factory
		 */
		public NormalFactory rotate() {
			return new NormalFactory(provider) {
				@Override
				public Normal vector(float theta, float phi) {
					return super.vector(theta - MathsUtility.HALF_PI, phi);
				}
			};
		}
	}
}
