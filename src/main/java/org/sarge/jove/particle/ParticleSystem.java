package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Model for a particle system.
 * @author Sarge
 */
public class ParticleSystem {
	private final PositionFactory pos;
	private final VectorFactory vec;
	private final float rate;
	private final int max;
	private final List<Particle> particles = new ArrayList<>();

	/**
	 * Constructor.
	 * @param pos		Position factory
	 * @param vec		Vector factory
	 * @param rate		Generation rate
	 * @param max		Maximum number of particles
	 */
	public ParticleSystem(PositionFactory pos, VectorFactory vec, float rate, int max) {
		this.pos = notNull(pos);
		this.vec = notNull(vec);
		this.rate = zeroOrMore(rate);
		this.max = oneOrMore(max);
	}

	/**
	 * @return Particles
	 */
	public Stream<Particle> particles() {
		return particles.stream();
	}

	/**
	 * Adds a new particle to this system.
	 */
	void add() {
		final Point start = pos.position();
		final Particle p = new Particle(start);
		p.add(vec.vector(start));
		particles.add(p);
	}

	/**
	 * Updates all particles in this system.
	 */
	void update() {
		particles.forEach(Particle::update);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Builder for a particle system.
	 * TODO
	 * - vector influences
	 * - others?
	 * - collision surfaces
	 * - lifetimes?
	 * - colours?
	 */
	public static class Builder {
		private PositionFactory pos = PositionFactory.ORIGIN;
		private VectorFactory vec = VectorFactory.literal(Vector.Y);
		private float rate = 1;
		private int num;
		private int max = Integer.MAX_VALUE;

		/**
		 * Sets the position factory for new particles generated by this system.
		 * @param pos Position factory
		 */
		public Builder position(PositionFactory pos) {
			this.pos = notNull(pos);
			return this;
		}

		/**
		 * Sets the vector factory for new particles generated by this system.
		 * @param vec Vector factory
		 */
		public Builder vector(VectorFactory vec) {
			this.vec = notNull(vec);
			return this;
		}

		/**
		 * Sets the rate at which new particles are generated by this system.
		 * @param rate Particle generation rate (per second) or zero to suppress particle generation
		 */
		public Builder rate(float rate) {
			this.rate = zeroOrMore(rate);
			return this;
		}

		/**
		 * Sets the initial number of particles in this system (default is zero).
		 * @param num Initial number of particles
		 * @see #max(int)
		 */
		public Builder initial(int num) {
			this.num = zeroOrMore(num);
			return this;
		}

		/**
		 * Sets the maximum number of particles in this system (default is unlimited).
		 * @param max Maximum number of particles
		 */
		public Builder max(int max) {
			this.max = zeroOrMore(max);
			return this;
		}

		/**
		 * Constructs this particle system.
		 * @return New particle system
		 * @throws IllegalArgumentException if the configured initial number of particles is larger than the configured maximum
		 */
		public ParticleSystem build() {
			final ParticleSystem sys = new ParticleSystem(pos, vec, rate, max);
			// TODO - initial
			return sys;
		}
	}
}
