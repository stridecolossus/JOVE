package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
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
	private Vector vec;

	/**
	 * Constructor.
	 * @param time		Creation time
	 * @param pos 		Starting position
	 * @param vec 		Initial movement vector
	 */
	protected Particle(long time, Point pos, Vector vec) {
		this.time = zeroOrMore(time);
		this.pos = notNull(pos);
		this.vec = notNull(vec);
	}

	/**
	 * @return Creation time
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
		return vec;
	}

	/**
	 * @return Whether this particle is idle
	 * @see #stop()
	 */
	public boolean isIdle() {
		return vec == null;
	}

	/**
	 * Stops this particle.
	 * @throws IllegalStateException if this particle has already been stopped
	 * @see #isIdle()
	 */
	void stop() {
		if(isIdle()) throw new IllegalStateException();
		vec = null;
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
	void destroy() {
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
		this.vec = this.vec.add(vec);
	}

	/**
	 * Modifies the <i>velocity</i> of this particle.
	 * @param v Velocity modifier
	 */
	public void velocity(float v) {
		this.vec = vec.multiply(v);
	}

	/**
	 * Reflects this particle at the given intersection.
	 * @param intersection Surface intersection
	 */
	void reflect(Point intersection, Vector normal) {
		// TODO - will be inaccurate?
		pos = notNull(intersection);
		vec = vec.reflect(normal);
	}

	/**
	 * Writes this particle to the given vertex buffer.
	 * @param bb Vertex buffer
	 */
	void buffer(ByteBuffer bb) {
		pos.buffer(bb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, pos, vec);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				(this.time == that.time) &&
				this.pos.equals(that.pos) &&
				this.vec.equals(that.vec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(pos).append(vec).build();
	}
}
