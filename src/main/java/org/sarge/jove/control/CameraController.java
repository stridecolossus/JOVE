package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.MathsUtility.*;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * The <i>camera controller</i> rotates the scene about the cameras position, i.e. a free-look controller.
 * @author Sarge
 */
public class CameraController {
	protected final Camera cam;
	private final Dimensions dim;
	private final NormalFactory factory;
//	private final Interpolator horizontal = Interpolator.linear(0, TWO_PI);
//	private final Interpolator vertical = Interpolator.linear(-HALF_PI, HALF_PI);
	// TODO - make interpolator ranges mutable?

	/**
	 * Constructor.
	 * @param camera 	Camera
	 * @param dim 		View dimensions
	 * @param factory	Sphere normals factory
	 */
	public CameraController(Camera camera, Dimensions dim, NormalFactory factory) {
		this.cam = requireNonNull(camera);
		this.dim = requireNonNull(dim);
		this.factory = factory.rotate();
	}

	public CameraController(Camera camera, Dimensions dim) {
		this(camera, dim, new NormalFactory());
	}

	/**
	 * Updates the camera for the given view coordinates.
	 * @see #update(Normal)
	 */
	public void update(float x, float y) {
// TODO - prepare inverse and multiply, move to helper?
//		final float yaw = horizontal.apply(x / dim.width());
//		final float pitch = vertical.apply(y / dim.height());
		final float yaw = lerp(x, 0, TWO_PI);
		final float pitch = lerp(y, -HALF_PI, +HALF_PI);
		final Vector vec = factory.vector(yaw, pitch);
		update(new Normal(vec));
	}

	// TODO
	static float lerp(float t, float start, float end) {
		// TODO - Math.fma(end - start, t, start);
		return start + (end - start) * t;
	}

//	/**
//	 * Updates the camera for the given position.
//	 * @param pos Position
//	 */
//	public void update(Position pos) {
//		update(pos.x(), pos.y());
//	}
//	// TODO - remove to bindings

	/**
	 * Updates the camera.
	 * @param dir View direction
	 */
	protected void update(Normal dir) {
		cam.direction(dir);
	}

	// TODO... [move]

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
