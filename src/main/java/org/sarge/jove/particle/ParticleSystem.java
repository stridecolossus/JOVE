package org.sarge.jove.particle;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sarge.jove.animation.AbstractPlayer;
import org.sarge.jove.app.FrameListener;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.Renderable;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictList;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.StrictSet;
import org.sarge.lib.util.ToString;

/**
 * Particle system manager.
 * TODO - separate into model and controller
 * @author Sarge
 */
public class ParticleSystem extends AbstractPlayer implements FrameListener, Renderable {
	/**
	 * Listener for particle system events.
	 */
	public static interface Listener {
		/**
		 * Notifies the given particle system has finished (zero particles and no more can be created).
		 * @param particleSystem Finished particle system
		 */
		void finished( ParticleSystem particleSystem );
	}

	/**
	 * Action for a collision surface.
	 */
	public static enum CollisionAction {
		/**
		 * Particle is culled.
		 */
		KILL,

		/**
		 * Particle sticks to the surface.
		 */
		STICK,

		/**
		 * Particle is reflected.
		 */
		REFLECT,
	}

	// Factories
	private final Emitter emitter;
	private final DirectionFactory dirFactory;
	private final ColourFactory colFactory;

	// State
	private PointGroup<Particle> group;
	private final List<Particle> particles = new StrictList<>();
	private final List<Influence> influences = new StrictList<>();
	private final Map<CollisionSurface, CollisionAction> surfaces = new StrictMap<>();
	private final Set<Listener> listeners = new StrictSet<>();

	// Config
	private Float creationRate;
	private Long ageLimit;
	private int max = Integer.MAX_VALUE;

	private float pending;				// Contribution towards creation of next particle(s)

	/**
	 * Constructor.
	 * @param emitter		Emitter for new particles
	 * @param dirFactory	Generates initial direction for new particles
	 * @param colFactory	Particle colour or <tt>null</tt> if un-coloured
	 */
	public ParticleSystem( Emitter emitter, DirectionFactory dirFactory, ColourFactory colFactory ) {
		Check.notNull( emitter );
		Check.notNull( dirFactory );

		this.emitter = emitter;
		this.dirFactory = dirFactory;
		this.colFactory = colFactory;
		//this.group = new PointGroup<>( colFactory != null, true, particles );
	}

	/**
	 * @return List of active particles
	 */
	public List<Particle> getParticles() {
		return particles;
	}

	/**
	 * Specifies the maximum number of particles to be automatically created on an update.
	 * Note that this limit does not apply if the particles are created explicitly via {@link #create(int, long)}.
	 * @param max Maximum number of particles
	 * @see #setCreationRate(float)
	 */
	public void setMaximumParticleCount( int max ) {
		Check.zeroOrMore( max );
		this.max = max;
	}

	/**
	 * Defines the maximum number of particles that can be created per unit-time.
	 * @param creationRate Number of particles to create per second or <tt>null</tt> to create as many as are needed on each iteration
	 * @see #setMaximumParticleCount(int)
	 */
	public void setCreationRate( Float creationRate ) {
		if( ( creationRate != null ) && ( creationRate <= 0 ) ) throw new IllegalArgumentException( "Creation rate must be positive" );
		this.creationRate = creationRate;
	}

	/**
	 * Sets the maximum age for particles.
	 * @param ageLimit Age limit (ms) or <tt>null</tt> if particles are never culled
	 * @see Particle#getCreationTime()
	 */
	public void setAgeLimit( Long ageLimit ) {
		if( ( ageLimit != null ) && ( ageLimit <= 0 ) ) throw new IllegalArgumentException( "Age limit must be positive" );
		this.ageLimit = ageLimit;
	}

	/**
	 * Adds a particle influence.
	 * @param inf Influence to add
	 */
	public void add( Influence inf ) {
		influences.add( inf );
	}

	/**
	 * Removes a particle influence.
	 * @param inf Influence to remove
	 */
	public void remove( Influence inf ) {
		influences.remove( inf );
	}

	/**
	 * Adds a particle-system listener.
	 * @param listener Listener
	 */
	public void add( Listener listener ) {
		listeners.add( listener );
	}

	/**
	 * Removes a particle-system listener.
	 * @param listener Listener
	 */
	public void remove( Listener listener ) {
		listeners.remove( listener );
	}

	/**
	 * Adds a collision surface.
	 * @param s			Surface
	 * @param action	Collision action
	 */
	public void add( CollisionSurface s, CollisionAction action ) {
		surfaces.put( s, action );
	}

	/**
	 * Removes a collision surface.
	 * @param s Surface
	 */
	public void remove( CollisionSurface s ) {
		surfaces.remove( s );
	}

	@Override
	public void update( long time, long elapsed ) {
		// Ignore if not running
		if( !isPlaying() ) return;

		// Cull expired particles
		if( ageLimit != null ) {
			for( Iterator<Particle> itr = particles.iterator(); itr.hasNext(); ) {
				final Particle p = itr.next();
				if( time - p.getCreationTime() > ageLimit ) {
					itr.remove();
				}
			}
		}

		// Apply influences
		for( Iterator<Particle> itr = particles.iterator(); itr.hasNext(); ) {
			final Particle p = itr.next();

			// Apply influences
			for( Influence inf : influences ) {
				inf.apply( p, elapsed );
			}

			// Check for collisions
			for( Entry<CollisionSurface, CollisionAction> entry : surfaces.entrySet() ) {
				final CollisionSurface surface = entry.getKey();
				if( surface.intersects( p ) ) {
					switch( entry.getValue() ) {
					case KILL:
						itr.remove();
						break;

					case STICK:
						p.setDirection( new Vector() );
						break;

					case REFLECT:
						p.setDirection( surface.reflect( p.getDirection() ) );
						break;
					}
				}
			}
		}

		// Update particle position
		final float speed = elapsed;
		for( Particle p : particles ) {
			p.update( speed );
		}

		// Create new particles as required
		if( particles.size() < max ) {
			// Determine number to create
			final int num;
			if( creationRate == null ) {
				// Create as many as needed to maintain maximum
				num = max - particles.size();
			}
			else {
				// Otherwise throttle number of new particles
				pending += elapsed / 1000f * creationRate;
				num = (int) Math.min( pending, max - particles.size() );
				if( num > 0 ) {
					pending -= num;
				}
			}

			// Create particles
			create( num, time );
		}

		if( particles.isEmpty() ) {
			// Notify finished particle system
			for( Listener listener : listeners ) {
				listener.finished( this );
			}

			// Stop if not repeating
			// TODO - how to test this since previous clause will CREATE more!
//			if( !isRepeating() ) {
//				setState( Player.State.STOPPED );
//			}
		}
	}

	@Override
	public void render( RenderContext ctx ) {
		group.render( ctx );
	}

	/**
	 * Creates new particles.
	 * @param num 		Number of new particles to create
	 * @param created	Creation time (ms)
	 */
	public void create( int num, long created ) {
		for( int n = 0; n < num; ++n ) {
			final Point pos = emitter.emit();
			final Vector dir = dirFactory.getDirection();
			final Colour col;
			if( colFactory == null ) {
				col = null;
			}
			else {
				col = colFactory.getColour();
			}
			final Particle p = new Particle( pos, dir, col, created );		// TODO - extension point? another factory?
			particles.add( p );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
