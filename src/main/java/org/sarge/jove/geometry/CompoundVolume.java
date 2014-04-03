package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Bounding volume comprised of multiple volumes that are tested in order.
 * @author Sarge
 */
public class CompoundVolume implements BoundingVolume {
	private final BoundingVolume[] volumes;

	/**
	 * Constructor.
	 * @param volumes List of volumes
	 */
	public CompoundVolume( BoundingVolume[] volumes ) {
		Check.notEmpty( volumes );
		this.volumes = volumes.clone();
	}

	@Override
	public Point getCentre() {
		return volumes[ 0 ].getCentre();
	}

	@Override
	public boolean contains( Point pt ) {
		for( BoundingVolume vol : volumes ) {
			if( !vol.contains( pt ) ) return false;
		}

		return true;
	}

	@Override
	public boolean intersects( Ray ray ) {
		for( BoundingVolume vol : volumes ) {
			if( !vol.intersects( ray ) ) return false;
		}

		return false;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
