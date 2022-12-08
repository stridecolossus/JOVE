package org.sarge.jove.scene.particle;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>particle</i> is a mutable record for the position and direction of a vertex in a particle system.
 * @author Sarge
 */
public class Particle implements Ray {
	private final long created;
	private Point pos;
	private Normal dir;
	private Colour col = Colour.WHITE;
	private float velocity = 1;

	/**
	 * Constructor.
	 * @param created		Creation timestamp
	 * @param pos 			Starting position
	 * @param dir			Initial direction
	 */
	protected Particle(long created, Point pos, Normal dir) {
		this.created = zeroOrMore(created);
		this.pos = notNull(pos);
		this.dir = notNull(dir);
	}

	/**
	 * @return Creation timestamp
	 */
	public long created() {
		return created;
	}

	@Override
	public Point origin() {
		return pos;
	}

	@Override
	public Normal direction() {
		return dir;
	}

	@Override
	public float length() {
		return velocity;
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
	 * Sets the direction of this particle.
	 * @param dir Particle direction
	 */
	public void direction(Normal dir) {
		this.dir = notNull(dir);
	}

	/**
	 * Modifies the velocity (or ray length) of this particle.
	 * @param velocity Velocity modifier
	 * @throws IllegalArgumentException if {@link #velocity} is zero
	 */
	public void velocity(float velocity) {
		if(MathsUtil.isZero(velocity)) throw new IllegalArgumentException();
		this.velocity *= velocity;
	}

	/**
	 * Reflects this particle at the given intersection.
	 * Note that this method does <b>not</b> take into account the distance travelled (or remaining) at the intersection.
	 * @param pos		Intersection point
	 * @param normal	Surface normal at this intersection
	 */
	public void reflect(Point pos, Normal normal) {
		this.pos = notNull(pos);
		this.dir = dir.reflect(normal).normalize();
	}

	/**
	 * Sets the colour of this particle.
	 * @param col Particle colour
	 */
	public void colour(Colour col) {
		this.col = notNull(col);
	}

	/**
	 * Writes this particle to the given buffer.
	 * @param bb Buffer
	 */
	void buffer(ByteBuffer bb) {
		// TODO - depends on layout, optionally also timestamp => function?
		pos.buffer(bb);
		col.buffer(bb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, pos, dir, col, velocity);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				(this.created == that.created) &&
				this.pos.equals(that.pos) &&
				this.dir.equals(that.dir) &&
				this.col.equals(that.col) &&
				MathsUtil.isEqual(this.velocity, that.velocity);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pos", pos)
				.append("dir", dir)
				.append("velocity", velocity)
				.append("col", col)
				.append("created", created)
				.build();
	}
}
