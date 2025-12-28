package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.control.Camera;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;

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

	/**
	 *
	 */
	public static class Builder {
		private final Viewport viewport;
		private final Camera camera;

		/**
		 * Constructor.
		 * @param viewport		Viewport
		 * @param camera		Camera
		 */
		public Builder(Viewport viewport, Camera camera) {
			this.viewport = requireNonNull(viewport);
			this.camera = requireNonNull(camera);
		}

		/**
		 * Builds the view frustum.
		 * @return Frustum
		 */
		public Frustum frustum() {
			return null;
		}
	}
}

	// http://davidlively.com/programming/graphics/frustum-calculation-and-culling-hopefully-demystified/

	////////////////////

//	Frustum createFrustumFromCamera(const Camera& cam, float aspect, float fovY,
//            float zNear, float zFar)
//{
//Frustum     frustum;
//const float halfVSide = zFar * tanf(fovY * .5f);
//const float halfHSide = halfVSide * aspect;
//const glm::vec3 frontMultFar = zFar * cam.Front;
//
//frustum.nearFace = { cam.Position + zNear * cam.Front, cam.Front };
//frustum.farFace = { cam.Position + frontMultFar, -cam.Front };
//frustum.rightFace = { cam.Position,
//glm::cross(frontMultFar - cam.Right * halfHSide, cam.Up) };
//frustum.leftFace = { cam.Position,
//glm::cross(cam.Up,frontMultFar + cam.Right * halfHSide) };
//frustum.topFace = { cam.Position,
//glm::cross(cam.Right, frontMultFar - cam.Up * halfVSide) };
//frustum.bottomFace = { cam.Position,
//glm::cross(frontMultFar + cam.Up * halfVSide, cam.Right) };
//
//return frustum;
//}

// unity...

// calc height at given distance:
//	var frustumHeight = 2.0f * distance * Mathf.Tan(camera.fieldOfView * 0.5f * Mathf.Deg2Rad);

// reverse operation to calc distance for a given height:
//  var distance = frustumHeight * 0.5f / Mathf.Tan(camera.fieldOfView * 0.5f * Mathf.Deg2Rad);

// calc FOV from height/distance:
//	var camera.fieldOfView = 2.0f * Mathf.Atan(frustumHeight * 0.5f / distance) * Mathf.Rad2Deg;

// helpers:
//	var frustumWidth = frustumHeight * camera.aspect;
//	var frustumHeight = frustumWidth / camera.aspect;

//	/**
//	 * Extracts a frustum from the given projection matrix.
//	 */
//	public static Frustum of(Matrix matrix) {
//		// Extract plane vectors
//		final Vector x = row(0, matrix);
//		final Vector y = row(1, matrix);
//		final Vector z = row(2, matrix);
//		final Vector w = row(3, matrix);
//
//		// Extract distances from the origin
//		final Vector dist = distance(matrix);
//		final float d = matrix.get(3, 3);
//
//		// TODO - extracting planes is now a bit messy with all the normals stuff
//
//		// Calc near/far planes
//		final Normal wz = new Normal(w.add(z));
//		final Plane near = new Plane(wz.invert(), d + dist.z);
//		final Plane far = new Plane(wz, d - dist.z);
//
//		// Left/right
//		final Vector negw = w.invert();
//		final Plane left = new Plane(new Normal(x.add(w)), d + dist.x);
//		final Plane right = new Plane(new Normal(x.add(negw).invert()), d - dist.x);
//
//		// Top/bottom
//		final Plane top = new Plane(new Normal(y.add(w)), d + dist.y);
//		final Plane bottom = new Plane(new Normal(y.add(negw).invert()), d - dist.y);
//
//		// Create frustum array
//		final Plane[] planes = {near, far, left, right, top, bottom};
//
//		// Normalize planes
//		for(int n = 0; n < planes.length; ++n) {
//			planes[n] = planes[n].normalize();
//		}
//
//		// Create frustum
//		return new Frustum(planes);
//	}
//
//	private static Vector row(int row, Matrix matrix) {
//		final float x = matrix.get(row, 0);
//		final float y = matrix.get(row, 1);
//		final float z = matrix.get(row, 2);
//		return new Vector(x, y, z);
//	}
//
//	private static Vector distance(Matrix matrix) {
//		final float x = matrix.get(0, 3);
//		final float y = matrix.get(1, 3);
//		final float z = matrix.get(2, 3);
//		return new Vector(x, y, z);
//	}
//}
