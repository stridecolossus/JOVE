package org.sarge.jove.particle;

import static java.util.stream.Collectors.toCollection;
import static org.sarge.lib.util.Check.*;

import java.util.*;
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
 * <li>test for collisions with surfaces according to {@link #add(Intersected, Collision)}</li>
 * <li>generate new particles according to the configured growth policy</li>
 * </ol>
 * <p>
 * @author Sarge
 */
public class ParticleSystem implements Animation {
	/**
	 * Characteristic hints for this particle system.
	 */
	public enum Characteristic {
		/**
		 * Whether particles should be culled.
		 */
		CULL,

		/**
		 * Whether particle creation timestamps are output to the vertex shader.
		 */
		TIMESTAMPS
	}

	private final Collection<Characteristic> chars;
	private PositionFactory position = PositionFactory.ORIGIN;
	private VectorFactory vector = VectorFactory.of(Vector.Y);
	private GenerationPolicy policy = GenerationPolicy.NONE;
	private final List<Influence> influences = new ArrayList<>();
	private final Map<Intersected, Collision> surfaces = new HashMap<>();
	private List<Particle> particles = new ArrayList<>();
	private long lifetime = Long.MAX_VALUE;

	/**
	 * Constructor.
	 * @param chars Particle system characteristics
	 */
	public ParticleSystem(Characteristic... chars) {
		this.chars = Arrays.asList(chars);
	}

	/**
	 * @return Characteristics of this particle system
	 */
	public Set<Characteristic> characteristics() {
		return Set.copyOf(chars);
	}

	/**
	 * Adds a particle system characteristic.
	 * @param c
	 * @return
	 */
	public ParticleSystem add(Characteristic c) {
		chars.add(notNull(c));
		return this;
	}

	/**
	 * @return Number of particles
	 */
	public synchronized int size() {
		return particles.size();
	}

	/**
	 * @return Particles
	 */
	protected synchronized List<Particle> particles() {
		return particles;
	}

	/**
	 * Sets the starting position for new particles (default is the origin).
	 * @param pos Starting position factory
	 */
	public ParticleSystem position(PositionFactory pos) {
		this.position = notNull(pos);
		return this;
	}

	/**
	 * Sets the initial movement vector for new particles (default is <i>up</i>).
	 * @param vec Movement vector factory
	 */
	public ParticleSystem vector(VectorFactory vec) {
		this.vector = notNull(vec);
		return this;
	}

	/**
	 * Adds new particles to this system.
	 * @param num 		Number of particles to add
	 * @param time		Current time
	 */
	public synchronized void add(int num, long time) {
		for(int n = 0; n < num; ++n) {
			final Point start = position.position();
			final Vector dir = vector.vector(start);
			final Particle p = new Particle(time, start, dir);
			particles.add(p);
		}
	}

	/**
	 * Sets the policy for the number of new particles to be generated on each frame (default is {@link GenerationPolicy#NONE}).
	 * @param policy Growth policy
	 */
	public ParticleSystem policy(GenerationPolicy policy) {
		this.policy = notNull(policy);
		return this;
	}

	/**
	 * @return Whether this particle system has a particle lifetime
	 */
	private boolean isLifetimeBound() {
		return lifetime < Long.MAX_VALUE;
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
	 * Adds a collision surface.
	 * @param surface		Surface
	 * @param action		Collision action
	 */
	public ParticleSystem add(Intersected surface, Collision action) {
		Check.notNull(surface);
		Check.notNull(action);
		surfaces.put(surface, action);
		return this;
	}

	/**
	 * Removes a collision surface.
	 * @param surface Surface to remove
	 */
	public ParticleSystem remove(Intersected surface) {
		surfaces.remove(surface);
		return this;
	}

	@Override
	public synchronized void update(Animator animator) {
		final Helper helper = new Helper(animator);
		helper.expire();
		helper.update();
		helper.cull();
		helper.generate();
	}

	/**
	 * Update helper.
	 */
	private class Helper {
		private final Animator animator;
		private final float pos;

		private Helper(Animator animator) {
			this.animator = animator;
			this.pos = animator.position();
		}

		/**
		 * Removes expired particles.
		 */
		void expire() {
			if(!isLifetimeBound()) {
				return;
			}

			final long expired = animator.time() - lifetime;

			particles
					.parallelStream()
					.filter(p -> p.time() < expired)
					.forEach(Particle::destroy);
		}

		/**
		 * Updates moving particles.
		 */
		void update() {
			particles
					.parallelStream()
					.filter(Particle::isAlive)
					.filter(Predicate.not(Particle::isIdle))
					.forEach(this::update);
		}

		/**
		 * Updates each particle.
		 */
		private void update(Particle p) {
			influence(p);
			move(p);
			collide(p);
		}

		/**
		 * Applies particle influences.
		 */
		private void influence(Particle p) {
			for(Influence inf : influences) {
				inf.apply(p, pos);
			}
		}

		/**
		 * Moves each particle.
		 */
		private void move(Particle p) {
			final Vector vec = p.direction().multiply(pos);
			p.move(vec);
		}

		/**
		 * Applies collision surfaces.
		 */
		private void collide(Particle p) {
			for(var entry : surfaces.entrySet()) {
				final Intersected surface = entry.getKey();
				final Intersection intersections = surface.intersection(p);
				if(!intersections.isEmpty()) {
					final Collision collision = entry.getValue();
					collision.collide(p, intersections);
					break;
				}
			}
		}

		/**
		 * Culls expired or destroyed particles.
		 */
		void cull() {
			if(!chars.contains(Characteristic.CULL) && !isLifetimeBound()) {
				return;
			}

			particles = particles
					.parallelStream()
					.filter(Particle::isAlive)
					.collect(toCollection(ArrayList::new));
		}

		/**
		 * Generates new particles according to the configured policy.
		 */
		void generate() {
			final int num = policy.count(particles.size(), pos); // TODO - elapsed * speed???
			if(num > 0) {
				add(num, animator.time());
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("count", size())
				.append(policy)
				.append("lifetime", lifetime)
				.append(position)
				.append(vector)
				.append("influences", influences.size())
				.append("surfaces", surfaces.size())
				.build();
	}
}
