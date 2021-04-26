package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Particle instance.
 * @author Sarge
 * TODO
 * - reflect
 * - is-a vertex?
 */
public class Particle {
	private static final Vector IDLE = new Vector(0, 0, 0);

	private Point pos;
	private Vector vec = IDLE;

	/**
	 * Constructor.
	 * @param pos Initial particle position
	 */
	public Particle(Point pos) {
		this.pos = notNull(pos);
	}

	/**
	 * @return Particle position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * @return Particle movement vector
	 */
	public Vector vector() {
		return vec;
	}

	/**
	 * Adds to this particles movement vector.
	 * @param vec Additional vector
	 */
	void add(Vector vec) {
		this.vec = this.vec.add(vec);
	}

	/**
	 * Stops this particle.
	 */
	void stop() {
		vec = IDLE;
	}

	/**
	 * Updates the position of this particle.
	 */
	void update() {
		pos = pos.add(vec);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
