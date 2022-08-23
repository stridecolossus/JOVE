package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.particle.CollisionSurface.Action;
import org.sarge.lib.util.Check;

/**
 * A <i>particle system</i> is a controller for a set of animated particles.
 * <p>
 * The {@link #policy(Policy)} configures the number of new particles to be generated on each frame.
 * Alternatively particles can be pre-allocated using the {@link #add(int)} method.
 * <p>
 * New particles are initialised according to the configured {@link #position(PositionFactory)} and {@link #vector(VectorFactory)} factories.
 * The {@link #particle(Point, Vector)} method should be overridden to implement custom particles or to implement pooling of particle instances.
 * <p>
 * On each frame all particles that are not {@link Particle#isStopped()} are updated as follows:
 * <ol>
 * <li>move each particle by its current vector</li>
 * <li>apply influences specified by {@link #add(Influence)}</li>
 * <li>test for collisions with surfaces according to {@link #add(CollisionSurface, Action)}</li>
 * <li>generate new particles according to the configured growth policy</li>
 * </ol>
 * <p>
 * TODO - age cull
 * @author Sarge
 */
public class ParticleSystem implements Animation {
	private static final Predicate<Particle> MOVING = Predicate.not(Particle::isStopped);

	/**
	 * A <i>particle system policy</i> specifies the number of particles to be generated on each frame.
	 */
	public interface Policy {
		/**
		 * Determines the number of particles to add on each frame.
		 * @param current Current number of particles
		 * @return New particles to generate
		 */
		int count(int current);

		/**
		 * Policy for a particle system that does not generate new particles.
		 */
		Policy NONE = ignored -> 0;

		/**
		 * Creates a policy that increments the number of particles (disregarding the current number of particles).
		 * @param inc Number of particles to generate
		 * @return Incremental policy
		 */
		static Policy increment(int inc) {
			return ignored -> inc;
		}

		/**
		 * Creates a policy adapter that caps the maximum number of particles.
		 * @param max Maximum number of particles
		 * @return New capped policy
		 */
		default Policy max(int max) {
			return current -> {
				final int count = Policy.this.count(current);
				return Math.min(max, count);
			};
		}
	}

	private PositionFactory pos = PositionFactory.ORIGIN;
	private VectorFactory vec = VectorFactory.of(Vector.Y);
	private Policy policy = Policy.NONE;
	private final Map<CollisionSurface, Action> surfaces = new HashMap<>();
	private final List<Influence> influences = new ArrayList<>();
	private final List<Particle> particles = new ArrayList<>();

	/**
	 * @return Particles
	 */
	public Stream<Particle> particles() {
		return particles(false);
	}

	/**
	 * @param moving Whether to exclude stopped particles
	 * @return Particles
	 */
	private Stream<Particle> particles(boolean moving) {
		if(moving) {
			return particles.stream().filter(MOVING);
		}
		else {
			return particles.stream();
		}
	}

	/**
	 * Adds new particles to this system.
	 * @param num Number of particles to add
	 */
	public void add(int num) {
		for(int n = 0; n < num; ++n) {
			final Point p = pos.position();
			final Vector v = vec.vector(p);
			final Particle particle = particle(p, v);
			particles.add(particle);
		}
	}

	/**
	 * Creates a new particle.
	 * Override for a custom particle implementation.
	 * @param pos Starting position
	 * @param vec Initial vector
	 * @return New particle
	 */
	protected Particle particle(Point pos, Vector vec) {
		return new Particle(pos, vec);
	}

	/**
	 * Sets the starting position for new particles (default is the origin).
	 * @param pos Starting position factory
	 */
	public ParticleSystem position(PositionFactory pos) {
		this.pos = notNull(pos);
		return this;
	}

	/**
	 * Sets the initial vector for new particles (default is <i>up</i>).
	 * @param vec Initial vector factory
	 */
	public ParticleSystem vector(VectorFactory vec) {
		this.vec = notNull(vec);
		return this;
	}

	/**
	 * Sets the policy for the number of new particles on each frame (default is {@link Policy#NONE}).
	 * @param policy Growth policy
	 */
	public ParticleSystem policy(Policy policy) {
		this.policy = notNull(policy);
		return this;
	}

	/**
	 * Adds a particle influence.
	 * @param influence Particle influence
	 */
	public ParticleSystem add(Influence influence) {
		influences.add(notNull(influence));
		return this;
	}

	/**
	 * Removes a particle influence.
	 * @param influence Particle influence to remove
	 */
	public ParticleSystem remove(Influence influence) {
		influences.remove(influence);
		return this;
	}

	/**
	 * Adds a collision surface.
	 * @param surface		Surface
	 * @param action		Action for collided particles
	 */
	public ParticleSystem add(CollisionSurface surface, Action action) {
		Check.notNull(surface);
		Check.notNull(action);
		surfaces.put(surface, action);
		return this;
	}

	/**
	 * Removes a collision surface.
	 * @param surface Surface to remove
	 */
	public ParticleSystem remove(CollisionSurface surface) {
		surfaces.remove(surface);
		return this;
	}

	@Override
	public void update(Animator animator) {
		final float elapsed = animator.position();
		influence();
		move();
		collide();
		generate();
	}

	/**
	 * Applies particle influences.
	 */
	private void influence() {
		for(Influence inf : influences) {
			final boolean moving = inf.ignoreStopped();
			particles(moving).forEach(inf::apply);
		}
	}

	/**
	 * Moves each particle.
	 */
	private void move() {
		particles(true).forEach(Particle::update);
	}

	/**
	 * Applies collision surfaces.
	 */
	private void collide() {
		for(var entry : surfaces.entrySet()) {
			// Enumerate intersected particles
			final CollisionSurface surface = entry.getKey();
			final List<Particle> intersected = particles(true)
					.filter(p -> surface.intersects(p.position()))
					.toList();

			// Apply action
			switch(entry.getValue()) {
				case DESTROY -> particles.removeAll(intersected);
				case STOP -> intersected.forEach(Particle::stop);
				case REFLECT -> {
					// TODO
				}
			}
		}
	}

	/**
	 * Generates new particles.
	 */
	private void generate() {
		final int count = policy.count(particles.size());
		if(count > 0) {
			add(count);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(vec)
				.append(policy)
				.append("count", particles.size())
				.build();
	}
}
