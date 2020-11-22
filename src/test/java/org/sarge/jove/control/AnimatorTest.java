package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Animator.Animation;

public class AnimatorTest {
	private Animator animator;
	private Animation animation;
//	private Frame frame;

	@BeforeEach
	public void before() {
		// Create animation
		animation = mock(Animation.class);
		animator = new Animator(5000L, animation);

//		// Init frame listener
//		frame = mock(Frame.class);
//		when(frame.elapsed()).thenReturn(1000L);

		// Start animation
		animator.apply(Player.State.PLAY);
	}

	@Test
	public void constructor() {
		assertEquals(0L, animator.time());
		assertEquals(5000L, animator.duration());
		assertEquals(1f, animator.speed(), 0.0001f);
	}

	@Test
	public void setSpeed() {
		animator.speed(2f);
		assertEquals(2f, animator.speed(), 0.0001f);
	}

	@Test
	public void update() {
//		animator.update(frame);
		animator.update(1000L);
		verify(animation).update(animator);
		assertEquals(1000L, animator.time());
	}

	@Test
	public void updateSpeed() {
		animator.speed(2f);
//		animator.update(frame);
		animator.update(1000L);
		assertEquals(2000L, animator.time());
	}

	@Test
	public void updateNotPlaying() {
		animator.apply(Player.State.STOP);
		animator.update(1000L);
//		animator.update(frame);
		verifyZeroInteractions(animation);
	}

	@Test
	public void updateFinished() {
//		when(frame.elapsed()).thenReturn(7500L);
//		animator.update(frame);
		animator.update(7500L);
		verify(animation).update(animator);
		assertEquals(false, animator.isPlaying());
		assertEquals(5000L, animator.time());
	}

	@Test
	public void updateRepeating() {
		animator.setRepeating(true);
		animator.update(7500L);
//		when(frame.elapsed()).thenReturn(7500L);
//		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(true, animator.isPlaying());
		assertEquals(2500L, animator.time());
	}
}
