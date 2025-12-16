package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.util.MathsUtility;

/**
 * The <i>sphere normal factory</i> generates surface normals on the unit sphere.
 * @author Sarge
 */
public interface SphereNormalFactory {
	/**
	 * Calculates the surface normal on the unit-sphere for the given rotation angles (radians, counter-clockwise).
	 * @param yaw		Horizontal angle in the range zero to {@link MathsUtility#TWO_PI}
	 * @param pitch		Vertical angle in the range +/- {@link MathsUtility#HALF_PI}
	 * @return Surface vector
	 */
	Normal normal(float yaw, float pitch);

	/**
	 * Adapts this factory by 'rotating' the <i>yaw</i> angle to point into {@code -Z} direction.
	 * @return Sphere normal factory orientated into the screen
	 */
	default SphereNormalFactory rotate() {
		return (yaw, pitch) -> normal(yaw - MathsUtility.HALF_PI, pitch);
	}

	/**
	 * Default implementation.
	 */
	class DefaultSphereNormalFactory implements SphereNormalFactory {
		private Cosine.Provider provider = Cosine.Provider.DEFAULT;

		/**
		 * Sets the cosine function used by this factory.
		 * @param provider Cosine function
		 */
		public void provider(Cosine.Provider provider) {
			this.provider = requireNonNull(provider);
		}

		@Override
		public Normal normal(float yaw, float pitch) {
			final Cosine theta = provider.cosine(yaw);
			final Cosine phi = provider.cosine(pitch);
			final float x = theta.cos() * phi.cos();
			final float y = phi.sin();
			final float z = theta.sin() * phi.cos();
			return new Normal(new Vector(x, y, z));
		}
	}
}
