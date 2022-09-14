package org.sarge.jove.particle;

import static java.util.stream.Collectors.toCollection;
import static org.sarge.lib.util.Check.*;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.Randomiser;
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
	private static final float SCALE = 1f / Frame.MILLISECONDS_PER_SECOND;

	/**
	 * Characteristic hints for this particle system.
	 */
	public enum Characteristic {
		/**
		 * Hint that particles in this system exist indefinitely, i.e. particles are not automatically expired after the configured {@link ParticleSystem#lifetime()}.
		 * Assumes particles are <b>not</b> destroyed by any configured collision surfaces.
		 */
		DISABLE_CULLING
	}

	// Particle properties
	private final Collection<Characteristic> chars;
	private PositionFactory position = PositionFactory.ORIGIN;
	private VectorFactory vector = VectorFactory.of(Axis.Y);
	private ColourFactory colour = ColourFactory.of(Colour.WHITE);
	private int max = 1;
	private long lifetime = Frame.MILLISECONDS_PER_SECOND;
	private List<Particle> particles = new ArrayList<>();

	// Controller
	private final Randomiser randomiser;
	private GenerationPolicy policy = GenerationPolicy.NONE;
	private final List<Influence> influences = new ArrayList<>();
	private final Map<Intersected, Collision> surfaces = new HashMap<>();

	/**
	 * Constructor.
	 * @param chars Particle system characteristics
	 */
	public ParticleSystem(Randomiser randomiser, Characteristic... chars) {
		this.randomiser = notNull(randomiser);
		this.chars = Arrays.asList(chars);
	}

	public ParticleSystem(Characteristic... chars) {
		this(new Randomiser(), chars);
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
	 * Sets the colour factory for new particles (default is {@link Colour#WHITE}).
	 * @param colour Colour factory
	 */
	public ParticleSystem colour(ColourFactory colour) {
		this.colour = notNull(colour);
		return this;
	}

	/**
	 * @return Maximum number of particles
	 */
	public int max() {
		return max;
	}

	/**
	 * Sets the maximum number of particles (default is one particle).
	 * @param max Maximum number of particles
	 */
	public ParticleSystem max(int max) {
		this.max = oneOrMore(max);
		return this;
	}

	/**
	 * Adds new particles to this system clamped by {@link #max(int)}.
	 * @param num 		Number of particles to add
	 * @param time		Current time
	 */
	public synchronized void add(int num, long time) {
		// Generate particles
		final int actual = Math.min(num, max - size());
		final List<Particle> added = new ArrayList<>(actual);
		for(int n = 0; n < actual; ++n) {
			final Point start = position.position();
			final Vector dir = vector.vector(start);
			final Particle p = particle(time, start, dir);
			added.add(p);
		}

		// Init particle colour if constant
		if(!colour.isModified()) {
			final Colour col = colour.colour(0);
			for(Particle p : added) {
				p.colour(col);
			}
		}

		// Add to system
		particles.addAll(added);
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
	 * Sets the particle lifetime (default is one second).
	 * @param lifetime Lifetime
	 */
	public ParticleSystem lifetime(Duration lifetime) {
		this.lifetime = oneOrMore(lifetime.toMillis());
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
		update(time, elapsed);
		cull();
		generate(time, elapsed * animator.speed());
	}

	/**
	 * Expire particles.
	 */
	private void expire(long time) {
		if(chars.contains(Characteristic.DISABLE_CULLING)) {
			return;
		}

		final long expiry = time - lifetime;

		particles
				.parallelStream()
				.filter(p -> p.created() < expiry)
				.forEach(Particle::destroy);
	}

	/**
	 * Update particles and apply collisions.
	 */
	private void update(long time, float elapsed) {
		/**
		 * Update instance.
		 */
		class Instance {
			/**
			 * Update each particle.
			 */
			private void update(Particle p) {
				if(colour.isModified()) {
					colour(p);
				}
				influence(p);
				move(p);
				collide(p);
			}

			/**
			 * Update the particle colour.
			 */
			private void colour(Particle p) {
				// TODO - means must have lifetime?
				final float t = (time - p.created()) / (float) lifetime;
				final Colour col = colour.colour(t);
				p.colour(col);
			}

			/**
			 * Apply particle influences.
			 */
			private void influence(Particle p) {
				for(Influence inf : influences) {
					inf.apply(p, elapsed);
				}
			}

			/**
			 * Move each particle.
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
		if(chars.contains(Characteristic.DISABLE_CULLING)) {
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
				.append("count", String.format("%d/%d", size(), max))
				.append(policy)
				.append("lifetime", lifetime)
				.append(position)
				.append(vector)
				.append(colour)
				.append("characteristics", chars)
				.append("influences", influences.size())
				.append("surfaces", surfaces.size())
				.build();
	}
}
