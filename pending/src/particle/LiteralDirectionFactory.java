package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Fixed direction factory.
 * @author Sarge
 */
public class LiteralDirectionFactory implements DirectionFactory {
	private final Vector dir;

	public LiteralDirectionFactory( Vector dir ) {
		Check.notNull( dir );
		this.dir = dir;
	}

	@Override
	public Vector getDirection() {
		return dir;
	}

	@Override
	public String toString() {
		return dir.toString();
	}
}
