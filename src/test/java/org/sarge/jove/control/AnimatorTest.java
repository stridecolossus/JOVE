package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Playable.State;

class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private FrameTimer frame;

	@BeforeEach
	void before() {
		frame = mock(FrameTimer.class);
		animation = mock(Animation.class);
		animator = new Animator(animation, Duration.ofSeconds(2));
	}

	@Test
	void constructor() {
		assertEquals(Duration.ofSeconds(2), animator.duration());
		assertEquals(Duration.ZERO, animator.time());
		assertEquals(animation, animator.animation());
		assertEquals(true, animator.isRepeating());
		assertEquals(1, animator.speed());
	}

	@DisplayName("A new animation...")
	@Nested
	class Stopped {
		@DisplayName("is initially stopped")
		@Test
		void isPlaying() {
			assertEquals(false, animator.isPlaying());
		}

		@DisplayName("is not updated on frame completion")
		@Test
		void update() {
			animator.update(frame);
			verifyNoMoreInteractions(animation);
		}

		@DisplayName("can be started")
		@Test
		void play() {
			animator.apply(State.PLAY);
			assertEquals(true, animator.isPlaying());
		}
	}

	@DisplayName("A running animation...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			animator.apply(State.PLAY);
		}

		@DisplayName("is updated on frame completion")
		@Test
		void update() {
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(1));
			animator.update(frame);
			assertEquals(Duration.ofSeconds(1), animator.time());
			assertEquals(true, animator.isPlaying());
			verify(animation).update(0.5f);
		}

		@DisplayName("has a position scaled by the animator speed")
		@Test
		void speed() {
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(2));
			animator.speed(0.5f);
			animator.update(frame);
			assertEquals(Duration.ofSeconds(1), animator.time());
			assertEquals(true, animator.isPlaying());
			assertEquals(0.5f, animator.speed());
			verify(animation).update(0.5f);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			animator.apply(State.STOP);
			animator.update(frame);
			assertEquals(false, animator.isPlaying());
			verifyNoMoreInteractions(animation);
		}
	}

	@DisplayName("An animation that has reached the end of the duration...")
	@Nested
	class Finished {
		@BeforeEach
		void before() {
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(3));
			animator.apply(State.PLAY);
		}

		@DisplayName("stops the animator if it is not repeated")
		@Test
		void finished() {
			animator.repeat(false);
			animator.update(frame);
			assertEquals(false, animator.isPlaying());
			assertEquals(Duration.ofSeconds(2), animator.time());
			verify(animation).update(1f);
		}

		@DisplayName("cycles the animation position if the animator is repeating")
		@Test
		void cycle() {
			animator.repeat(true);
			animator.update(frame);
			assertEquals(true, animator.isPlaying());
			verify(animation).update(0.5f);
		}
	}
}
