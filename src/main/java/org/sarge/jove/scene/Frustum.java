package org.sarge.jove.scene;

import java.util.Arrays;
import java.util.List;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>frustum</i> is a camera clipping space specified as an array of planes.
 * <p>
 * Generally a frustum is visualised as a truncated pyramid with the top/bottom at the near/far viewing planes.
 * Note that the normals of each plane are assumed to point <i>inside</i> the frustum, i.e. a frustum is the {@link Plane.HalfSpace#POSITIVE} half-space of its planes.
 * <p>
 * For convenience a frustum is also defined as a bounding {@link Volume} to allow volume implementations to perform custom intersection tests.
 * However note that {@link #intersect(Ray)} is not implemented.
 * <p>
 * A frustum can be created
 * TODO
 * <p>
 * @author Sarge
 */
public class Frustum implements Volume {
	private final Plane[] planes;

	/**
	 * Constructor.
	 * Generally a frustum is comprised of six planes (one for each side of the truncated pyramid) but this is not enforced by this constructor.
	 * @param planes Frustum planes
	 */
	public Frustum(Plane[] planes) {
		this.planes = Arrays.copyOf(planes, planes.length);
	}

	/**
	 * @return Frustum planes
	 */
	public List<Plane> planes() {
		return Arrays.asList(planes);
	}

	@Override
	public boolean contains(Point pt) {
		for(Plane p : planes) {
			if(p.space(pt) == Plane.HalfSpace.NEGATIVE) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean intersects(Volume vol) {
		if(vol instanceof SphereVolume sphere) {
			return intersects(sphere);
		}
		else
		if(vol instanceof BoundingBox box) {
			// TODO
			return false;
		}
		else {
			return vol.intersects(this);
		}
	}

	private boolean intersects(SphereVolume sphere) {
		final float r = sphere.radius() * sphere.radius();
		final Point centre = sphere.centre();
		for(Plane p : planes) {
			if(p.distance(centre) > r) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Intersection intersect(Ray ray) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Frustum that) && Arrays.equals(this.planes, that.planes);
	}

	@Override
	public String toString() {
		return planes.toString();
	}

	// http://davidlively.com/programming/graphics/frustum-calculation-and-culling-hopefully-demystified/

	public static Frustum of(Point pos, Vector dir, float near, float far, float height, float w) {

		// TODO
		// - Scene class?
		// - test compare this with of()


		return null;
	}

	/**
	 * Extracts a frustum from the given matrix.
	 * <p>
	 * For a given matrix M comprised of the projection matrix P and modelview matrix MV the space of the resultant frustum clipping planes is as follows:
	 * <p>
	 * <table border=1>
	 * <tr>
	 * 	<th>matrix (M)</th>
	 * 	<th>space</th>
	 * </tr>
	 * <tr>
	 * 	<td>identity</td>
	 * 	<td>NDC</td>
	 * </tr>
	 * <tr>
	 * 	<td>P</td>
	 * 	<td>view (or camera) space</td>
	 * </tr>
	 * <tr>
	 * 	<td>P * MV</td>
	 * 	<td>model space</td>
	 * </tr>
	 * </table>
	 * @param matrix Matrix
	 * @return Frustum
	 */
	public static Frustum of(Matrix matrix) {
		// Extract plane vectors
		final Vector x = matrix.row(0);
		final Vector y = matrix.row(1);
		final Vector z = matrix.row(2);
		final Vector w = matrix.row(3);

		// Extract distances from the origin
		final Vector dist = matrix.column(3);
		final float d = matrix.get(3, 3);

		// Calc near/far planes
		final Vector wz = w.add(z);
		final Plane near = new Plane(wz.negate(), d + dist.z);
		final Plane far = new Plane(wz, d - dist.z);

		// Left/right
		final Vector negw = w.negate();
		final Plane left = new Plane(x.add(w), d + dist.x);
		final Plane right = new Plane(x.add(negw).negate(), d - dist.x);

		// Top/bottom
		final Plane top = new Plane(y.add(w), d + dist.y);
		final Plane bottom = new Plane(y.add(negw).negate(), d - dist.y);

		// Create frustum array
		final Plane[] planes = {near, far, left, right, top, bottom};

		// Normalize planes
		for(int n = 0; n < planes.length; ++n) {
			planes[n] = planes[n].normalize();
		}

		// Create frustum
		return new Frustum(planes);
	}
}
