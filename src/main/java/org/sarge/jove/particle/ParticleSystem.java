package org.sarge.jove.particle;

import static java.util.stream.Collectors.toCollection;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
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
	private static final float SCALE = 1f / TimeUnit.SECONDS.toMillis(1);

	/**
	 * Characteristic hints for this particle system.
	 */
	public enum Characteristic {
		/**
		 * Whether particles should be culled, i.e. the system has influences or collisions surfaces that {@link Particle#destroy()} particles.
		 * Ignored if the particle system has a {@link ParticleSystem#lifetime(long)}.
		 */
		CULL,

		/**
		 * Whether particles output the creation {@link Particle#time()} to the vertex buffer.
		 */
		TIMESTAMPS
	}

	private final Collection<Characteristic> chars;
	private PositionFactory position = PositionFactory.ORIGIN;
	private VectorFactory vector = VectorFactory.of(Axis.Y);
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
	 * @param c Characteristic
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
	 * Sets the initial movement vector for new particles (default is {@link Vector#Y}).
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
	 * @see #factory(Factory)
	 */
	public synchronized void add(int num, long time) {
		for(int n = 0; n < num; ++n) {
			final Point start = position.position();
			final Vector dir = vector.vector(start);
			final Particle p = particle(time, start, dir);
			particles.add(p);
		}
	}

	/**
	 * Creates a new particle instance.
	 * Override for custom particle sub-classes or to implement a particle pool.
	 * @param time		Creation timestamp
	 * @param pos		Starting position
	 * @param dir		Initial movement direction
	 * @return New particle
	 */
	protected Particle particle(long time, Point pos, Vector dir) {
		return new Particle(time, pos, dir);
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
	 * @return Particle lifetime
	 */
	public long lifetime() {
		return lifetime;
	}

	/**
	 * @return Whether this particle system has a particle lifetime
	 */
	private boolean isLifetimeBound() {
		return lifetime < Long.MAX_VALUE;
	}

	/**
	 * Sets the particle lifetime (default is indefinite).
	 * @param lifetime Lifetime (ms)
	 * @see Characteristic#CULL
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
	 * @see Characteristic#CULL
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
		final Frame frame = animator.frame();
		final long time = frame.time().toEpochMilli();
		final float elapsed = frame.elapsed().toMillis() * SCALE;
		expire(time);
		update(elapsed);
		cull();
		generate(time, elapsed * animator.speed());
	}

	/**
	 * Expire particles.
	 */
	private void expire(long time) {
		if(!isLifetimeBound()) {
			return;
		}

		final long expired = time - lifetime;

		particles
				.parallelStream()
				.filter(p -> p.time() < expired)
				.forEach(Particle::destroy);
	}

	/**
	 * Update particles and apply collisions.
	 */
	private void update(float elapsed) {
		/**
		 * Update instance.
		 */
		class Instance {
			/**
			 * Update each particle.
			 */
			private void update(Particle p) {
				influence(p);
				move(p);
				collide(p);
			}

			/**
			 * Apply particle influences.
			 * TODO - redundant if change to trajectory
			 */
			private void influence(Particle p) {
				for(Influence inf : influences) {
					inf.apply(p, elapsed);
				}
			}

			/**
			 * Move each particle.
			 * TODO - move to particle if change to trajectory
			 */
			private void move(Particle p) {
				final Vector vec = p.direction().multiply(elapsed);
				p.move(vec);
			}
		}

		final Instance instance = new Instance();
		particles
				.parallelStream()
				.filter(Particle::isAlive)
				.filter(Predicate.not(Particle::isIdle))
				.forEach(instance::update);
	}

	/**
	 * Apply collision surfaces.
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
	 * Cull expired or destroyed particles
	 */
	private void cull() {
		if(!chars.contains(Characteristic.CULL) && !isLifetimeBound()) {
			return;
		}

		particles = particles
				.parallelStream()
				.filter(Particle::isAlive)
				.collect(toCollection(ArrayList::new));
	}

	/**
	 * Generate new particles according to the configured policy.
	 */
	private void generate(long time, float elapsed) {
		final int num = policy.count(particles.size(), elapsed);
		if(num > 0) {
			add(num, time);
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
