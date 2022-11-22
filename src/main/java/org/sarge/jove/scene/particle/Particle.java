package org.sarge.jove.scene.particle;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;

/**
 * A <i>particle</i> is a mutable record for the position and direction of a vertex in a particle system.
 * @author Sarge
 */
public class Particle implements Ray {
	private final long created;
	private Point pos;
	private Vector dir;
	private Colour col = Colour.WHITE;

	/**
	 * Constructor.
	 * @param created		Creation timestamp
	 * @param pos 			Starting position
	 * @param dir			Initial direction
	 */
	protected Particle(long created, Point pos, Vector dir) {
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
	public void reflect(Point pos, Normal normal) {
		this.pos = notNull(pos);
		this.dir = dir.reflect(normal);
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
		return Objects.hash(created, pos, dir, col);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				(this.created == that.created) &&
				this.pos.equals(that.pos) &&
				this.dir.equals(that.dir) &&
				this.col.equals(that.col);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pos", pos)
				.append("dir", dir)
				.append("col", col)
				.append("created", created)
				.build();
	}
}
