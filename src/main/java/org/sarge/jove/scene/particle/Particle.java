package org.sarge.jove.scene.particle;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>particle</i> is a mutable record for the position and direction of a vertex in a particle system.
 * @author Sarge
 */
public class Particle implements Bufferable {
	private final long created;
	private Ray ray;
	private Colour col = Colour.WHITE;
	private float velocity = 1;

	/**
	 * Constructor.
	 * @param created		Creation timestamp
	 * @param pos 			Starting position
	 * @param dir			Initial direction
	 */
	protected Particle(long created, Point pos, Normal dir) {
		this.created = requireZeroOrMore(created);
		this.ray = new Ray(pos, dir);
	}

	/**
	 * @return Creation timestamp
	 */
	public long created() {
		return created;
	}

	/**
	 * @return This particle as a ray
	 */
	public Ray ray() {
		return ray;
	}

	/**
	 * @return Whether this particle is idle
	 * @see #stop()
	 */
	public boolean isIdle() {
		return velocity == 0;
	}

	/**
	 * Stops this particle.
	 * @param pos Particle position
	 * @throws IllegalStateException if this particle has already been stopped
	 * @see #isIdle()
	 */
	public void stop() {
		if(isIdle()) throw new IllegalStateException();
		velocity = 0;
	}

	/**
	 * Stops this particle at the given position (usually an intersection point at a collision).
	 * @param pos Stopped position
	 * @throws IllegalStateException if this particle has already been stopped
	 * @see #stop()
	 */
	public void stop(Point pos) {
		stop();
		ray = new Ray(pos, ray.direction());
	}

	/**
	 * @return Whether this particle is still alive
	 * @see #destroy()
	 */
	public boolean isAlive() {
		return ray != null;
	}

	/**
	 * Kills this particle.
	 * @throws IllegalStateException if this particle has already been killed
	 * @see #isAlive()
	 */
	public void destroy() {
		if(!isAlive()) throw new IllegalStateException();
		ray = null;
	}

	/**
	 * Moves this particle by the given vector.
	 * @param vector Vector
	 */
	public void move(Vector vector) {
		final Point pos = new Point(ray.origin()).add(vector);
		ray = new Ray(pos, ray.direction());
	}

	/**
	 * Sets the direction of this particle.
	 * @param dir Particle direction
	 */
	public void direction(Normal dir) {
		ray = new Ray(ray.origin(), dir);
	}

	/**
	 * Modifies the velocity (or ray length) of this particle.
	 * @param velocity Velocity modifier
	 * @throws IllegalArgumentException if {@link #velocity} is zero
	 */
	public void velocity(float velocity) {
		if(MathsUtility.isApproxZero(velocity)) throw new IllegalArgumentException();
		this.velocity = this.velocity * velocity;
	}

	/**
	 * Reflects this particle at the given intersection.
	 * Note that this method does <b>not</b> take into account the distance travelled (or remaining) at the intersection.
	 * @param pos		Intersection point
	 * @param normal	Surface normal at this intersection
	 */
	public void reflect(Point pos, Normal normal) {
		final Vector dir = ray.direction().reflect(normal);
		ray = new Ray(pos, dir);
	}

	/**
	 * @return Particle colour
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * Sets the colour of this particle.
	 * @param col Particle colour
	 */
	public void colour(Colour col) {
		this.col = requireNonNull(col);
	}

	@Override
	public void buffer(ByteBuffer bb) {
		// TODO - depends on layout, optionally also timestamp => function?
		ray.origin().buffer(bb);
		col.buffer(bb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, ray, col, velocity);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Particle that) &&
				(this.created == that.created) &&
				Objects.equals(this.ray, that.ray) &&
				this.col.equals(that.col) &&
				MathsUtility.isApproxEqual(this.velocity, that.velocity);
	}
}
