package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.*;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * A <i>particle</i> is a mutable record for the position and direction of a vertex in a particle system.
 * @author Sarge
 */
public class Particle implements Ray {
	private final long time;
	private Point pos;
	private Vector dir;

	/**
	 * Constructor.
	 * @param time		Creation timestamp
	 * @param pos 		Starting position
	 * @param dir		Initial direction
	 */
	protected Particle(long time, Point pos, Vector dir) {
		this.time = zeroOrMore(time);
		this.pos = notNull(pos);
		this.dir = notNull(dir);
	}

	/**
	 * @return Creation timestamp
	 */
	public long time() {
		return time;
	}

	@Override
	public Point origin() {
		return pos;
	}

	@Override
	public Vector direction() {
		return dir;
	}

	/**
	 * @return Whether this particle is idle
	 * @see #stop()
	 */
	public boolean isIdle() {
		return dir == null;
	}

	/**
	 * Stops this particle.
	 * @param pos Particle position
	 * @throws IllegalStateException if this particle has already been stopped
	 * @see #isIdle()
	 */
	public void stop() {
		if(isIdle()) throw new IllegalStateException();
		dir = null;
	}

	/**
	 * Stops this particle at the given position (usually an intersection point at a collision).
	 * @param pos Stopped position
	 * @throws IllegalStateException if this particle has already been stopped
	 * @see #stop()
	 */
	public void stop(Point pos) {
		stop();
		this.pos = notNull(pos);
	}

	/**
	 * @return Whether this particle is still alive
	 * @see #destroy()
	 */
	public boolean isAlive() {
		return pos != null;
	}

	/**
	 * Kills this particle.
	 * @throws IllegalStateException if this particle has already been killed
	 * @see #isAlive()
	 */
	public void destroy() {
		if(!isAlive()) throw new IllegalStateException();
		pos = null;
	}

	/**
	 * Moves this particle by the given vector.
	 * @param vec Vector
	 */
	public void move(Vector vec) {
		pos = pos.add(vec);
	}

	/**
	 * Combines the given vector with the direction of this particle.
	 * @param vec Direction modifier
	 */
	public void add(Vector vec) {
		dir = dir.add(vec);
	}

	/**
	 * Modifies the <i>velocity</i> of this particle.
	 * @param v Velocity modifier
	 */
	public void velocity(float v) {
		dir = dir.multiply(v);
	}

	/**
	 * Reflects this particle at the given intersection.
	 * Note that this method does <b>not</b> take into account the distance travelled (or remaining) at the intersection.
	 * @param pos		Intersection point
	 * @param normal	Surface normal at this intersection
	 */
	public void reflect(Point pos, Vector normal) {
		this.pos = notNull(pos);
		this.dir = dir.reflect(normal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, pos, dir);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				(this.time == that.time) &&
				this.pos.equals(that.pos) &&
				this.dir.equals(that.dir);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(dir)
				.append("created", time)
				.build();
	}
}
