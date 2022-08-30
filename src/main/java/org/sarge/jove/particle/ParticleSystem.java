package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * A <i>particle system</i> is a controller for a particle animation.
 * <p>
 * The {@link #policy(Policy)} configures the number of new particles to be generated on each frame.
 * Alternatively particles can be pre-allocated using the {@link #add(int, long)} method.
 * <p>
 * New particles are initialised according to the configured {@link #position(PositionFactory)} and {@link #vector(VectorFactory)} factories.
 * The maximum lifetime of a particle can be configured via {@link #lifetime(long)}.
 * <p>
 * On each frame all particles that are not {@link Particle#isIdle()} are updated as follows:
 * <ol>
 * <li>apply influences specified by {@link #add(Influence)}</li>
 * <li>move each particle by its current vector</li>
 * <li>test for collisions with surfaces according to {@link #add(Intersects, CollisionAction)}</li>
 * <li>generate new particles according to the configured growth policy</li>
 * </ol>
 * <p>
 * @author Sarge
 */
public class ParticleSystem implements Animation {
	private PositionFactory pos = PositionFactory.ORIGIN;
	private VectorFactory vec = VectorFactory.of(Vector.Y);
	private Policy policy = Policy.NONE;
	private final List<Influence> influences = new ArrayList<>();
	private final Map<Intersects, CollisionAction> surfaces = new HashMap<>();
	private List<Particle> particles = List.of();
	private long lifetime = Long.MAX_VALUE;

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
	 * Adds new particles to this system.
	 * @param num 		Number of particles to add
	 * @param time		Current time
	 */
	public void add(int num, long time) {
		for(int n = 0; n < num; ++n) {
			final Point start = pos.position();
			final Vector dir = vec.vector(start);
			final Particle p = new Particle(time, start, dir);
			particles.add(p);
		}
	}

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

	/**
	 * Sets the policy for the number of new particles to be generated on each frame (default is {@link Policy#NONE}).
	 * @param policy Growth policy
	 */
	public ParticleSystem policy(Policy policy) {
		this.policy = notNull(policy);
		return this;
	}

	/**
	 * Sets the particle lifetime (default is forever).
	 * @param lifetime Lifetime (ms)
	 */
	public ParticleSystem lifetime(long lifetime) {
		this.lifetime = oneOrMore(lifetime);
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
	 * Action for particles that intersect a collision surface.
	 */
	public enum CollisionAction {
		/**
		 * Particle is destroyed.
		 */
		DESTROY,

		/**
		 * Particle stops, i.e. sticks to the surface.
		 * @see Particle#stop()
		 */
		STOP,

		/**
		 * Particle is reflected.
		 * @see Particle#reflect(Intersection)
		 */
		REFLECT
	}

	/**
	 * Adds a collision surface.
	 * @param surface		Surface
	 * @param action		Action for collided particles
	 */
	public ParticleSystem add(Intersects surface, CollisionAction action) {
		Check.notNull(surface);
		Check.notNull(action);
		surfaces.put(surface, action);
		return this;
	}

	/**
	 * Removes a collision surface.
	 * @param surface Surface to remove
	 */
	public ParticleSystem remove(Intersects surface) {
		surfaces.remove(surface);
		return this;
	}

	@Override
	public boolean update(Animator animator) {
		final Helper helper = new Helper(animator);
		helper.cull();
		helper.update();
		helper.generate();
		return false;
	}

	/**
	 * Update helper.
	 */
	private class Helper {
		private static final float SECONDS = 1f / TimeUnit.SECONDS.toMillis(1);

		private final long time;
		private final long expired;
		private final float elapsed;

		private Helper(Animator animator) {
			this.time = animator.time();
			this.expired = time - lifetime;
			this.elapsed = animator.elapsed() * SECONDS;
		}

		/**
		 * Culls expired particles.
		 */
		void cull() {
			particles = particles
					.parallelStream()
					.filter(p -> p.time() > expired)
					.toList();
		}

		/**
		 * Updates particles and removes any destroyed by collisions.
		 */
		void update() {
			particles = particles
					.parallelStream()
					.filter(Predicate.not(Particle::isIdle))
					.peek(this::influence)
					.peek(this::move)
					.peek(this::collide)
					.filter(Particle::isAlive)
					.toList();
		}

		/**
		 * Applies particle influences.
		 */
		private void influence(Particle p) {
			for(Influence inf : influences) {
				inf.apply(p, elapsed);
			}
		}

		/**
		 * Moves each particle.
		 */
		private void move(Particle p) {
			final Vector vec = p.direction().multiply(elapsed);
			p.move(vec);
		}

		/**
		 * Applies collision surfaces.
		 */
		private void collide(Particle p) {
			for(var entry : surfaces.entrySet()) {
				final Intersects surface = entry.getKey();
				final Iterator<Intersection> intersections = surface.intersections(p);
				if(!intersections.hasNext()) {
					continue;
				}

				// Apply action for intersected particles
				switch(entry.getValue()) {
					case DESTROY -> p.destroy();
					case STOP -> p.stop();
					case REFLECT -> {
						final Intersection pt = surface.intersections(p).next();
						p.reflect(pt);
					}
				}
			}
		}

		/**
		 * Generates new particles according to the configured policy.
		 */
		void generate() {
			final float num = policy.count(size()) * elapsed;
			if(num > 0) {
				add((int) num, time);
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(policy)
				.append(pos)
				.append(vec)
				.append("count", size())
				.build();
	}
}
