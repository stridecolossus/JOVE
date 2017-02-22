package org.sarge.jove.scene;

import java.util.Optional;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Light.
 * @author Sarge
 */
public class Light {
	/**
	 * Creates an ambient light.
	 * @param col Colour of this ambient light
	 * @return Ambient light
	 */
	public static Light ambient(Colour col) {
		return new Light(col, null, null);
	}
	
	/**
	 * Creates a directional light.
	 * @param col Colour of this light
	 * @param dir Direction
	 * @return Directional light
	 */
	public static Light directional(Colour col, Vector dir) {
		return new Light(col, dir, null);
	}
	
	/**
	 * Creates a point-light.
	 * @param col Colour of this light
	 * @param pos Light position
	 * @return Point-light
	 */
	public static Light point(Colour col, Point pos) {
		return new Light(col, null, pos);
	}
	
	/**
	 * Creates a spot-light.
	 * @param col Colour of this light
	 * @param dir Direction
	 * @param pos Light position
	 * @return Spot-light
	 */
	public static Light spotLight(Colour col, Vector dir, Point pos) {
		return new Light(col, dir, pos);
	}
// TODO
//		cutoff
//		exponent
//		attenuation: constant, linear, quadratic

	private final Colour col;
	private final Optional<Vector> dir;
	private final Optional<Point> pos;

	/**
	 * constructor.
	 * @param col colour
	 * @param dir Direction
	 * @param pos Position
	 */
	private Light(Colour col, Vector dir, Point pos) {
		Check.notNull(col);
		this.col = col;
		this.dir = Optional.ofNullable(dir);
		this.pos = Optional.ofNullable(pos);
	}
	
	/**
	 * @return Colour of this light
	 */
	public Colour getColour() {
		return col;
	}

	/**
	 * @return Direction of this light
	 */
	public Optional<Vector> getDirection() {
		return dir;
	}

	/**
	 * @return Position of this light
	 */
	public Optional<Point> getPosition() {
		return pos;
	}
	
	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("col", col);
		dir.ifPresent(d -> ts.append("dir", d));
		pos.ifPresent(p -> ts.append("pos", p));
		return ts.toString();
	}
}
