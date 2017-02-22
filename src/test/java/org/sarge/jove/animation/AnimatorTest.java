package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.Animator.Animation;
import org.sarge.jove.util.MathsUtil;

public class AnimatorTest {
	private static final long DURATION = 5000;
	
	private Animator animator;
	private Animation animation;

	@Before
	public void before() {
		animation = mock(Animation.class);
		animator = new Animator(animation, 5000L);
		animator.play();
	}

	@Test
	public void constructor() {
		assertFloatEquals(1f, animator.getSpeed());
		assertEquals(0, animator.getTime());
	}

	@Test
	public void update() {
		final long elapsed = 2500;
		animator.update(0, elapsed);
		assertEquals(elapsed, animator.getTime());
		verify(animation).update(elapsed, DURATION);
	}

	@Test
	public void updateRepeating() {
		final long elapsed = 7500;
		animator.update(0, elapsed);
		assertEquals(elapsed % DURATION, animator.getTime());
		verify(animation).update(elapsed % DURATION, DURATION);
	}

	@Test
	public void updateNotRepeating() {
		final long elapsed = 7500;
		animator.setRepeating(false);
		animator.update(0, elapsed);
		assertEquals(DURATION, animator.getTime());
		assertEquals(false, animator.isPlaying());
		verify(animation).update(DURATION, DURATION);
	}

	@Test
	public void setSpeed() {
		final long elapsed = 5000;
		animator.setSpeed(MathsUtil.HALF);
		animator.update(0, elapsed);
		assertEquals(elapsed / 2, animator.getTime());
		verify(animation).update(elapsed / 2, DURATION);
	}

	@Test
	public void notPlaying() {
		animator.pause();
		animator.update(0, 2500L);
		assertEquals(0, animator.getTime());
		verifyZeroInteractions(animation);
	}
}
