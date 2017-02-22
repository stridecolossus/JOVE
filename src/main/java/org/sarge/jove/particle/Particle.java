package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Particle instance in a {@link ParticleSystem}.
 * @author Sarge
 */
public class Particle {
	private final long created;

	private Point pos;
	private Vector dir;
	private Colour col;
	private float alpha = 1;

	/**
	 * Constructor.
	 * @param pos		Initial particle position
	 * @param dir		Initial vector
	 * @param col		Colour (optional)
	 * @param created	Creation time (ms)
	 */
	public Particle(Point pos, Vector dir, Colour col, long created) {
		Check.notNull(pos);
		Check.notNull(dir);
		this.dir = dir;
		this.col = col;
		this.created = created;
	}

	/**
	 * Updates the position of this particle.
	 * @param speed Speed scalar
	 */
	public void update(float speed) {
		pos = pos.add(dir.multiply(speed));
	}
	
	/**
	 * @return Particle position
	 */
	public Point getPosition() {
		return pos;
	}

	/**
	 * Sets the position of this particle.
	 * @param p Position
	 */
	public void setPosition(Point p) {
		Check.notNull(p);
		pos = p;
	}

	/**
	 * @return Current direction vector
	 */
	public Vector getDirection() {
		return dir;
	}

	/**
	 * Sets the particle direction.
	 * @param dir Direction
	 */
	public void setDirection(Vector dir) {
		Check.notNull(dir);
		this.dir = dir;
	}

	/**
	 * Adds the given vector to the current direction of this particle.
	 * @param vec Movement vector
	 */
	public void add(Vector vec) {
		dir = dir.add(vec);
	}

	/**
	 * @return Creation time of this particle (ms)
	 */
	public long getCreationTime() {
		return created;
	}

	/**
	 * @return Particle colour
	 */
	public Colour getColour() {
		return col;
	}
	
	/**
	 * @return Colour alpha
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * Sets the alpha value of this particle.
	 * @param alpha Alpha value
	 */
	public void setAlpha(float alpha) {
		Check.isPercentile(alpha);
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
