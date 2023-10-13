package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;

class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private Frame frame;
	private long elapsed;

	@BeforeEach
	void before() {
		frame = new Frame() {
			@Override
			public Duration elapsed() {
				return Duration.ofSeconds(elapsed);
			}
		};
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
			animator.play();
			assertEquals(true, animator.isPlaying());
		}
	}

	@DisplayName("A running animation...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			animator.play();
		}

		@DisplayName("is updated on frame completion")
		@Test
		void update() {
			elapsed = 1;
			animator.update(frame);
			assertEquals(Duration.ofSeconds(1), animator.time());
			assertEquals(true, animator.isPlaying());
			verify(animation).update(0.5f);
		}

		@DisplayName("has a position scaled by the animator speed")
		@Test
		void speed() {
			elapsed = 2;
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
			animator.stop();
			animator.update(frame);
			assertEquals(false, animator.isPlaying());
			verifyNoMoreInteractions(animation);
		}
	}

	@DisplayName("An animation that has reached the end of its duration...")
	@Nested
	class Finished {
		@BeforeEach
		void before() {
			elapsed = 3;
			animator.play();
		}

		@DisplayName("stops if it does not repeat")
		@Test
		void finished() {
			animator.repeat(false);
			animator.update(frame);
			assertEquals(false, animator.isPlaying());
			assertEquals(Duration.ofSeconds(2), animator.time());
			verify(animation).update(1f);
		}

		@DisplayName("cycles the animation if it is repeating")
		@Test
		void cycle() {
			animator.repeat(true);
			animator.update(frame);
			assertEquals(true, animator.isPlaying());
			verify(animation).update(0.5f);
		}
	}
}
