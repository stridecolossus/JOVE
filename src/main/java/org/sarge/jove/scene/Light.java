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
public final class Light {
	/**
	 * Creates an ambient light-source.
	 * @param col Colour
	 * @return Ambient light-source
	 */
	public static Light ambient(Colour col) {
		return new Light(col, null, null);
	}

	/**
	 * Creates a directional light-source, e.g. sunlight
	 * @param col Colour
	 * @param dir Direction
	 * @return Directional light-source
	 */
	public static Light directional(Colour col, Vector dir) {
		Check.notNull(col);
		return new Light(col, dir, null);
	}

	/**
	 * Creates a point-source light, e.g. a bulb
	 * @param col Colour
	 * @param pos Position
	 * @return Point-source light
	 */
	public static Light pointSource(Colour col, Point pos) {
		Check.notNull(col);
		return new Light(col, null, pos);
	}

	/**
	 * Creates a spot-light.
	 * @param col Colour
	 * @param pos Position
	 * @param dir Direction
	 * @return Spot-light
	 */
	public static Light spotLight(Colour col, Point pos, Vector dir) {
		Check.notNull(col);
		Check.notNull(col);
		return new Light(col, dir, pos);
		// TODO
		//		cutoff
		//		exponent
		//		attenuation: constant, linear, quadratic
	}
	
	private final Colour col;
	private final Optional<Vector> dir;
	private final Optional<Point> pos;

	/**
	 * Constructor.
	 * @param col Colour
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
	 * @return Position of this light
	 */
	public Optional<Point> getPosition() {
		return pos;
	}

	/**
	 * @return Direction of this light
	 */
	public Optional<Vector> getDirection() {
		return dir;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
