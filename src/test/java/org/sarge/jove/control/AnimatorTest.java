package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Player.State;

class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private FrameTracker frame;

	@BeforeEach
	void before() {
		// Create animation
		animation = mock(Animation.class);
		animator = new Animator(5000, animation);

		// Init frame listener
		frame = mock(FrameTracker.class);
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(1));
	}

	@Test
	void constructor() {
		assertEquals(State.STOP, animator.state());
		assertEquals(false, animator.isRepeating());
		assertEquals(0, animator.time());
		assertEquals(5000, animator.duration());
		assertEquals(1, animator.speed());
	}

	@Test
	void speed() {
		animator.speed(2);
		assertEquals(2, animator.speed());
	}

	@Test
	void update() {
		animator.state(State.PLAY);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(1000, animator.time());
	}

	@Test
	void updateSpeed() {
		animator.state(State.PLAY);
		animator.speed(2);
		animator.update(frame);
		assertEquals(2000, animator.time());
	}

	@Test
	void updateNotPlaying() {
		animator.update(frame);
		verifyNoMoreInteractions(animation);
	}

	@Test
	void updateFinished() {
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(6));
		animator.state(State.PLAY);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(State.STOP, animator.state());
		assertEquals(5000, animator.time());
	}

	@Test
	void updateRepeating() {
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(6));
		animator.state(State.PLAY);
		animator.repeat(true);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(State.PLAY, animator.state());
		assertEquals(1000, animator.time());
	}
}
