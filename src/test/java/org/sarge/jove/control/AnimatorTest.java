package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Player.State;

class AnimatorTest {
	private Animator animator;
	private Animation animation;

	@BeforeEach
	void before() {
		animation = mock(Animation.class);
		animator = new Animator(5000, animation);
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
		animator.frame(Instant.EPOCH, Instant.ofEpochSecond(1));
		verify(animation).update(animator);
		assertEquals(1000, animator.time());
	}

	@Test
	void updateSpeed() {
		animator.state(State.PLAY);
		animator.speed(2);
		animator.frame(Instant.EPOCH, Instant.ofEpochSecond(1));
		assertEquals(2000, animator.time());
	}

	@Test
	void updateNotPlaying() {
		animator.frame(Instant.EPOCH, Instant.ofEpochSecond(1));
		verifyNoMoreInteractions(animation);
	}

	@Test
	void updateFinished() {
		animator.state(State.PLAY);
		animator.frame(Instant.EPOCH, Instant.ofEpochSecond(6));
		verify(animation).update(animator);
		assertEquals(State.STOP, animator.state());
		assertEquals(5000, animator.time());
	}

	@Test
	void updateRepeating() {
		animator.state(State.PLAY);
		animator.repeat(true);
		animator.frame(Instant.EPOCH, Instant.ofEpochSecond(6));
		verify(animation).update(animator);
		assertEquals(State.PLAY, animator.state());
		assertEquals(1000, animator.time());
	}
}
