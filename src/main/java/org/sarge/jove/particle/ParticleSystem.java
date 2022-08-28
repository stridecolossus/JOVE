package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray.Intersection;
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
 * TODO
 * <p>
 * On each frame all particles that are not {@link Particle#isStopped()} are updated as follows:
 * <ol>
 * <li>TODO
 * move each particle by its current vector</li>
 * <li>apply influences specified by {@link #add(Influence)}</li>
 * <li>test for collisions with surfaces according to {@link #add(CollisionSurface, Action)}</li>
 * <li>generate new particles according to the configured growth policy</li>
 * </ol>
 * <p>
 * TODO - age cull
 * @author Sarge
 */
public class ParticleSystem implements Animation {
	private static final Predicate<Particle> MOVING = Predicate.not(Particle::isIdle);

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
				return Math.min(count, max - current);
			};
		}
	}

	private PositionFactory pos = PositionFactory.ORIGIN;
	private VectorFactory vec = VectorFactory.of(Vector.Y);
	private Policy policy = Policy.NONE;
	private final List<Influence> influences = new ArrayList<>();
	private final Map<CollisionSurface, Action> surfaces = new HashMap<>();
	private final List<Particle> particles = new ArrayList<>();

	/**
	 * @return Number of particles
	 */
	public int size() {
		return particles.size();
	}

	/**
	 * @return Particles
	 */
	List<Particle> particles() {
		return particles;
	}

	/**
	 * Adds new particles to this system.
	 * @param num Number of particles to add
	 */
	public void add(int num) {
		for(int n = 0; n < num; ++n) {
			final Point start = pos.position();
			final Vector dir = vec.vector(start);
			final Particle particle = new Particle(start, dir);
			particles.add(particle);
		}
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
	 * Sets the initial movement vector for new particles (default is <i>up</i>).
	 * @param vec Movement vector factory
	 */
	public ParticleSystem vector(VectorFactory vec) {
		this.vec = notNull(vec);
		return this;
	}

	/**
	 * Sets the policy for the number of new particles to be generated on each frame (default is {@link Policy#NONE}).
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
		final long elapsed = animator.elapsed();
		influence(elapsed);
		move();
		collide();
		generate();
	}

	/**
	 * Applies particle influences.
	 */
	private void influence(long elapsed) {
		for(Influence inf : influences) {
			particles.stream().forEach(p -> inf.apply(p, elapsed));
		}
	}

	/**
	 * Moves each particle.
	 */
	private void move() {
		particles.stream().filter(MOVING).forEach(Particle::update);
	}

	/**
	 * Applies collision surfaces.
	 */
	private void collide() {
		for(var entry : surfaces.entrySet()) {
			final CollisionSurface surface = entry.getKey();
			final Action action = entry.getValue();
			final Iterator<Particle> itr = particles.iterator();
			while(itr.hasNext()) {
				// Ignore stopped particles
				final Particle p = itr.next();
				if(p.isIdle()) {
					continue;
				}

				// Apply action for intersecting particles
				if(surface.intersects(p.origin())) {
					switch(action) {
						case DESTROY -> itr.remove();
						case STOP -> p.stop();
						case REFLECT -> {
							final Intersection pt = surface.intersections(p).next();
							p.reflect(pt);
						}
					}
				}
			}
		}
	}

	/**
	 * Generates new particles according to the configured policy.
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
				.append(policy)
				.append(pos)
				.append(vec)
				.append("count", particles.size())
				.build();
	}
}
