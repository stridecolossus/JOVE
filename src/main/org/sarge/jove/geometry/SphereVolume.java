package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Sphere volume.
 * @author Sarge
 */
public class SphereVolume implements BoundingVolume {
	private Point centre;
	private float radius;

	/**
	 * Default constructor for point volume.
	 */
	public SphereVolume() {
		this( Point.ORIGIN, 0 );
	}

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public SphereVolume( Point centre, float radius ) {
		Check.notNull( centre );
		Check.zeroOrMore( radius );

		this.centre = centre;
		this.radius = radius;
	}

	/**
	 * Constructor for a sphere at the origin.
	 * @param radius Radius
	 */
	public SphereVolume( float radius ) {
		this( new Point(), radius );
	}

	@Override
	public Point getCentre() {
		return centre;
	}

	@Override
	public void setCentre( Point centre ) {
		Check.notNull( centre );
		this.centre = centre;
	}

	public float getRadius() {
		return radius;
	}

	@Override
	public void scale( float scale ) {
		radius *= scale;
	}

	public boolean contains( Point pos ) {
		return pos.distanceSquared( centre ) <= radius * radius;
	}

	@Override
	public boolean intersects( Ray ray ) {
		// Build vector from sphere to ray origin
		final Vector vec = centre.subtract( ray.getOrigin() );
		System.out.print(vec);

		// Determine distance to sphere
		final float d;
		if( vec.dot( ray.getDirection() ) < 0 ) {
			// Sphere is behind ray origin
			d = vec.getMagnitudeSquared();
			System.out.print(" behind" );
		}
		else {
			// Project sphere centre onto ray
			final Vector proj = ray.getDirection().project( centre );
			final Vector pv = centre.subtract( proj );
			System.out.print(" "+pv);
			d = pv.getMagnitudeSquared();
		}

		// Check distance to sphere centre
		System.out.println(" dist="+d+" r="+(radius*radius) );
		return d <= radius * radius;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
