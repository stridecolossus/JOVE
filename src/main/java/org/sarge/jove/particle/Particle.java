package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * A <i>particle</i> is a mutable record for the position and direction of a vertex in a particle system.
 * @author Sarge
 */
public class Particle implements Ray {
	private Point pos;
	private Vector vec;

	/**
	 * Constructor.
	 * @param pos Starting position
	 * @param vec Initial movement vector
	 */
	protected Particle(Point pos, Vector vec) {
		this.pos = notNull(pos);
		this.vec = notNull(vec);
	}

	/**
	 * Copy constructor.
	 * @param p Particle to copy
	 */
	protected Particle(Particle p) {
		this(p.pos, p.vec);
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
	 * Updates the position of this particle by its direction.
	 */
	void update() {
		pos = pos.add(vec);
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
	 * Reflects this particle at the given surface intersection.
	 * @param intersection Surface intersection
	 */
	void reflect(Intersection intersection) {
		// Move back to previous position
		final Point prev = pos.add(vec.invert());

		// Calculate intersection point
		final Vector inc = vec.multiply(intersection.distance());
		pos = prev.add(inc);

		// Reflect about normal
		vec = vec.reflect(intersection.normal());
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
		return Objects.hash(pos, vec);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				this.pos.equals(that.pos) &&
				this.vec.equals(that.vec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(pos).append(vec).build();
	}
}
