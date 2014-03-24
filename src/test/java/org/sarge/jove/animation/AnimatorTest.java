package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.animation.Player.State;

public class AnimatorTest  {
	private Animator animator;
	private Animation animation;

	@Before
	public void before() {
		animation = mock( Animation.class );
		when( animation.getDuration() ).thenReturn( 5000L );
		when( animation.getMinimum() ).thenReturn( 3f );
		when( animation.getMaximum() ).thenReturn( 5f );
		animator = new Animator( animation );
		animator.setState( State.PLAYING );
	}

	@Test
	public void constructor() {
		assertFloatEquals( 1f, animator.getSpeed() );
		assertEquals( 0, animator.getTime() );
	}

	private void check( float expected ) {
		final ArgumentCaptor<Float> captor = ArgumentCaptor.forClass( Float.class );
		verify( animation ).update( captor.capture() );
		assertFloatEquals( expected, captor.getValue() );
	}

	@Test
	public void update() {
		animator.update( 0, 2500L );
		assertEquals( 2500L, animator.getTime() );
		check( 4f );
	}

	@Test
	public void updateRepeating() {
		animator.update( 0, 7500L );
		assertEquals( 2500L, animator.getTime() );
		check( 4f );
	}

	@Test
	public void updateNotRepeating() {
		animator.setRepeating( false );
		animator.update( 0, 6000L );
		assertEquals( 5000L, animator.getTime() );
		assertEquals( false, animator.isPlaying() );
		check( 5f );
	}

	@Test
	public void setSpeed() {
		animator.setSpeed( 0.5f );
		animator.update( 0, 5000L );
		assertEquals( 2500L, animator.getTime() );
		check( 4f );
	}

	@Test
	public void notPlaying() {
		animator.setState( State.PAUSED );
		animator.update( 0, 2500L );
		assertEquals( 0, animator.getTime() );
		verifyZeroInteractions( animation );
	}
}
