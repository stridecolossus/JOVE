package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Animator.Animation;

public class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private FrameTracker frame;

	@BeforeEach
	public void before() {
		// Create animation
		animation = mock(Animation.class);
		animator = new Animator(Duration.ofSeconds(5), animation);

		// Init frame listener
		frame = mock(FrameTracker.class);
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(1));

		// Start animation
		animator.apply(Player.State.PLAY);
	}

	@Test
	public void constructor() {
		assertEquals(0, animator.time());
		assertEquals(TimeUnit.SECONDS.toNanos(5), animator.duration());
		assertEquals(1, animator.speed());
	}

	@Test
	public void setSpeed() {
		animator.speed(2);
		assertEquals(2, animator.speed());
	}

	@Test
	public void update() {
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(TimeUnit.SECONDS.toNanos(1), animator.time());
	}

	@Test
	public void updateSpeed() {
		animator.speed(2);
		animator.update(frame);
		assertEquals(TimeUnit.SECONDS.toNanos(2), animator.time());
	}

	@Test
	public void updateNotPlaying() {
		animator.apply(Player.State.STOP);
		animator.update(frame);
		verifyNoMoreInteractions(animation);
	}

	@Test
	public void updateFinished() {
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(6));
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(false, animator.isPlaying());
		assertEquals(TimeUnit.SECONDS.toNanos(5), animator.time());
	}

	@Test
	public void updateRepeating() {
		when(frame.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(6));
		animator.setRepeating(true);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(true, animator.isPlaying());
		assertEquals(TimeUnit.SECONDS.toNanos(1), animator.time());
	}
}
