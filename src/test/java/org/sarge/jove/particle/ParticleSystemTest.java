package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.Player.State;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.particle.ParticleSystem.CollisionAction;
import org.sarge.jove.particle.ParticleSystem.Listener;

public class ParticleSystemTest {
	private ParticleRenderer renderer;
	private ParticleSystem sys;

	@Before
	public void before() {
		renderer = mock( ParticleRenderer.class );

		sys = new ParticleSystem(
			new PointEmitter( new Point( 1, 2, 3 ) ),
			new LiteralDirectionFactory( new Vector( 4, 5, 6 ) ),
			LiteralColourFactory.WHITE
		);

		sys.setState( State.PLAYING );
		sys.setMaximumParticleCount( 0 );
	}

	@Test
	public void constructor() {
		assertNotNull( sys.getParticles() );
		assertTrue( sys.getParticles().isEmpty() );
	}

	@Test
	public void create() {
		// Explicitly create a particle1
		sys.create( 1, 42L );
		assertEquals( 1, sys.getParticles().size() );

		// Check created particle
		final Particle p = sys.getParticles().iterator().next();
		assertNotNull( p );
		assertEquals( new Point( 1, 2, 3 ), p.getPosition() );
		assertEquals( new Vector( 4, 5, 6 ), p.getDirection() );
		assertEquals( Colour.WHITE, p.getColour() );
		assertEquals( 42L, p.getCreationTime() );
	}

	@Test
	public void updateAutoCreation() {
		sys.setMaximumParticleCount( 1 );
		sys.update( 42L, 0 );
		assertEquals( 1, sys.getParticles().size() );
	}

	@Test
	public void updateAutoCreationRate() {
		// Define system that creates a new particle every 2 seconds
		sys.setMaximumParticleCount( 100 );
		sys.setCreationRate( 0.5f );

		// Update and check no particles
		sys.update( 0L, 1000L );
		assertEquals( 0, sys.getParticles().size() );

		// Update again and check particle created
		sys.update( 0L, 2000L );
		assertEquals( 1, sys.getParticles().size() );
	}

	@Test
	public void updateParticlePosition() {
		sys.create( 1, 0L );
		sys.update( 0L, 1L );
		assertEquals( new Vector( 5, 7, 9 ), sys.getParticles().iterator().next().getPosition() );
	}

	@Test
	public void updateWithParticleInfluence() {
		// Create a particle
		sys.create( 1, 0L );
		final Particle p = sys.getParticles().iterator().next();

		// Attach an influence
		final Influence inf = mock( Influence.class );
		sys.add( inf );

		// Update and check influence applied to particle
		sys.update( 0L, 42L );
		verify( inf ).apply( p, 42L );
	}

	@Test
	public void cullAgedParticles() {
		// Create system with particle aging
		sys.create( 1, 0L );
		sys.setAgeLimit( 1L );

		// Update one tick and check still active
		sys.update( 1L, 1L );
		assertEquals( 1, sys.getParticles().size() );

		// Update past age and check culled
		sys.update( 3L, 1L );
		assertEquals( 0, sys.getParticles().size() );
	}

	@Test
	public void emptyParticleSystem() {
		// Attach a listener
		final Listener listener = mock( Listener.class );
		sys.add( listener );

		// Update empty system and check listener notified
		sys.setMaximumParticleCount( 0 );
		sys.update( 42L, 42L );
		verify( listener ).finished( sys );
	}

	@Test
	public void collisionSurfaceStick() {
		collide( CollisionAction.STICK );
		assertEquals( 1, sys.getParticles().size() );
		assertEquals( new Point( 5, 7, 9 ), sys.getParticles().iterator().next().getPosition() );
	}

	@Test
	public void collisionSurfaceReflect() {
		collide( CollisionAction.REFLECT );
		assertEquals( 1, sys.getParticles().size() );
		assertEquals( Vector.Z_AXIS, sys.getParticles().iterator().next().getDirection() );
	}

	@Test
	public void collisionSurfaceKill() {
		collide( CollisionAction.KILL );
		assertEquals( "Expected particle to be killed", 0, sys.getParticles().size() );
	}

	private void collide( CollisionAction action ) {
		// Attach a collision surface
		final CollisionSurface surface = mock( CollisionSurface.class );
		when( surface.reflect( any( Vector.class ) ) ).thenReturn( Vector.Z_AXIS );
		sys.add( surface, action );

		// Create a particle and check initially not colliding
		sys.create( 1, 0L );
		sys.update( 0L, 1L );
		assertEquals( 1, sys.getParticles().size() );

		// Mock collision
		final Particle p = sys.getParticles().iterator().next();
		when( surface.intersects( p ) ).thenReturn( true );
		sys.update( 1L, 0L );
	}

	@Test
	public void render() {
		sys.create( 1, 0L );
		sys.render( null );
		final Particle p = sys.getParticles().iterator().next();
		verify( renderer ).render( Collections.singletonList( p ), null );
	}
}
