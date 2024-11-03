package org.sarge.jove.scene.core;

import java.util.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.volume.Volume;

/**
 * A <i>frustum</i> is a clipping space specified as an array of planes.
 * <p>
 * Generally a frustum is visualised as a truncated pyramid with the top/bottom at the near/far viewing planes.
 * Note that the normals of each plane point to the <i>inside</i> the frustum, i.e. a frustum is the {@link HalfSpace#POSITIVE} half-space of its planes.
 * <p>
 * A frustum can be extracted from a projection or modelview matrix using the {@link #of(Matrix)} factory method.
 * <p>
 * Alternatively TODO constructs a frustum from TODO
 * <p>
 * @author Sarge
 */
public class Frustum {
	private final Plane[] planes;

	/**
	 * Constructor.
	 * Generally a frustum is comprised of six planes (one for each side of the truncated pyramid) but note this is <b>not</b> enforced by this constructor.
	 * @param planes Frustum clipping planes
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

	/**
	 * Tests whether this volume contains the given point.
	 * @param pt Point
	 * @return Whether contained
	 * @see Plane#halfspace(Point)
	 */
	public boolean contains(Point pt) {
		for(Plane p : planes) {
			if(p.halfspace(pt) == HalfSpace.NEGATIVE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests whether this frustum is intersected by the given bounding volume.
	 * @param vol Bounding volume
	 * @return Whether intersects
	 * @see Volume#intersects(Plane)
	 */
	public boolean intersects(Volume vol) {
		for(Plane p : planes) {
			if(!vol.intersects(p)) {
				return false;
			}
		}
		return true;
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
	 * Extracts a frustum from the given projection matrix.
	 * <p>
	 * For a given matrix M comprised of the projection matrix P and modelview matrix MV the space of the resultant frustum clipping planes is as follows:
	 *
	 * TODO - nasty javadoc
	 *
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
		final Vector x = row(0, matrix);
		final Vector y = row(1, matrix);
		final Vector z = row(2, matrix);
		final Vector w = row(3, matrix);

		// Extract distances from the origin
		final Vector dist = distance(matrix);
		final float d = matrix.get(3, 3);

		// TODO - extracting planes is now a bit messy with all the normals stuff

		// Calc near/far planes
		final Normal wz = new Normal(w.add(z));
		final Plane near = new Plane(wz.invert(), d + dist.z);
		final Plane far = new Plane(wz, d - dist.z);

		// Left/right
		final Vector negw = w.invert();
		final Plane left = new Plane(new Normal(x.add(w)), d + dist.x);
		final Plane right = new Plane(new Normal(x.add(negw).invert()), d - dist.x);

		// Top/bottom
		final Plane top = new Plane(new Normal(y.add(w)), d + dist.y);
		final Plane bottom = new Plane(new Normal(y.add(negw).invert()), d - dist.y);

		// Create frustum array
		final Plane[] planes = {near, far, left, right, top, bottom};

		// Normalize planes
		for(int n = 0; n < planes.length; ++n) {
			planes[n] = planes[n].normalize();
		}

		// Create frustum
		return new Frustum(planes);
	}

	private static Vector row(int row, Matrix matrix) {
		final float x = matrix.get(row, 0);
		final float y = matrix.get(row, 1);
		final float z = matrix.get(row, 2);
		return new Vector(x, y, z);
	}

	private static Vector distance(Matrix matrix) {
		final float x = matrix.get(0, 3);
		final float y = matrix.get(1, 3);
		final float z = matrix.get(2, 3);
		return new Vector(x, y, z);
	}
}
