package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.*;

/**
 * A <i>particle</i> is a model for an element of a particle system.
 * @author Sarge
 */
public class Particle implements Bufferable {
	private Point pos;
	private Vector vec;

	/**
	 * Constructor.
	 * @param pos Starting position
	 * @param vec Initial vector
	 */
	public Particle(Point pos, Vector vec) {
		this.pos = notNull(pos);
		this.vec = notNull(vec);
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
	Vector vector() {
		return vec;
	}

	/**
	 * @return Whether this particle has been stopped
	 * @see #stop()
	 */
	public boolean isStopped() {
		return vec == null;
	}

	/**
	 * Adds to this particles movement vector.
	 * @param v Additional vector
	 * @throws IllegalStateException if this particle has been stopped
	 */
	public void add(Vector v) {
		check();
		vec = vec.add(v);
	}

	/**
	 * Stops this particle.
	 * @see CollisionSurface.Action#STOP
	 * @throws IllegalStateException if this particle has already been stopped
	 */
	public void stop() {
		check();
		vec = null;
	}

	/**
	 * Updates the position of this particle.
	 * @throws NullPointerException if this particle has been stopped
	 */
	void update() {
		pos = pos.add(vec);
	}

	@Override
	public int length() {
		return Point.LAYOUT.length();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		pos.buffer(bb);
	}

	private void check() {
		if(isStopped()) throw new IllegalStateException();
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
				Objects.equals(this.vec, that.vec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pos", pos)
				.append("vec", vec)
				.build();
	}
}
