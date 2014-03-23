package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sarge.jove.animation.Player.State;
import org.sarge.jove.util.MockitoTestCase;

public class AnimatorTest extends MockitoTestCase {
	private Animator animator;
	private @Mock Animation animation;

	@Before
	public void before() {
		animator = new Animator( animation, 5000L );
	}

	@Test
	public void constructor() {
		assertEquals( Player.State.STOPPED, animator.getState() );
		assertFloatEquals( 1f, animator.getSpeed() );
		assertEquals( 0, animator.getTime() );
	}

	@Test
	public void update() {
		// Set to half-way and check updated correctly
		animator.setState( Player.State.PLAYING );
		animator.update( 0, 2500L );
		assertEquals( 2500L, animator.getTime() );

		// Wrap past end and check updated
		animator.update( 0, 3500L );
		assertEquals( 1000L, animator.getTime() );
	}

	@Test
	public void notRepeating() {
		animator.setRepeating( false );
		animator.setState( State.PLAYING );
		animator.update( 0, 6000L );
		assertEquals( 5000L, animator.getTime() );
		assertEquals( false, animator.isPlaying() );
	}

	@Test
	public void setSpeed() {
		animator.setState( Player.State.PLAYING );
		animator.setSpeed( 0.5f );
		animator.update( 0, 2000L );
		assertEquals( 1000L, animator.getTime() );
	}

	@Test
	public void notPlaying() {
		animator.update( 0, 2500L );
		assertEquals( 0, animator.getTime() );
	}
}
