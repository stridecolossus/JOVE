package org.sarge.jove.light;

import org.sarge.jove.geometry.Point;
import org.sarge.lib.util.Check;

/**
 * Point light (3).
 * @author Sarge
 */
public class PointLight extends AbstractLight {
	private Point pos = Point.ORIGIN;

	@Override
	public int getType() {
		return 3;
	}

	/**
	 * @return Light position
	 */
	public Point getPosition() {
		return pos;
	}

	/**
	 * Sets the light position.
	 * @param pos Light position
	 */
	public void setPosition( Point pos ) {
		Check.notNull( pos );
		this.pos = pos;
	}
}
