package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.State;

class AnimatorTest {
	private Animator animator;
	private AtomicReference<Float> animation;
	private Instant instant;

	@BeforeEach
	void before() {
		instant = Instant.now();
		animation = new AtomicReference<>();
		animator = new Animator(animation::set, Duration.ofSeconds(8));
	}

	@Test
	void constructor() {
		assertEquals(Duration.ofSeconds(8), animator.duration());
		assertEquals(true, animator.isRepeating());
		assertEquals(1, animator.speed());
	}

	@DisplayName("A new animator...")
	@Nested
	class New {
		@DisplayName("is not playing")
		@Test
		void zero() {
			assertEquals(false, animator.isPlaying());
		}

		@DisplayName("is at the start of the animation")
		@Test
		void start() {
			assertEquals(Duration.ZERO, animator.time());
			assertEquals(null, animation.get());
		}

		@DisplayName("ignores frame updates")
		@Test
		void stopped() {
			animator.frame(new Frame(instant, instant.plusSeconds(1)));
			assertEquals(Duration.ZERO, animator.time());
			assertEquals(null, animation.get());
		}
	}

	@DisplayName("A running animator...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			animator.state(State.PLAYING);
		}

		@DisplayName("updates the animation time on each frame")
		@Test
		void frame() {
			animator.frame(new Frame(instant, instant.plusSeconds(4)));
			assertEquals(true, animator.isPlaying());
			assertEquals(Duration.ofSeconds(4), animator.time());
			assertEquals(0.5f, animation.get());
		}

		@DisplayName("can configure the update speed")
		@Test
		void speed() {
			animator.speed(2);
			animator.frame(new Frame(instant, instant.plusSeconds(2)));
			assertEquals(true, animator.isPlaying());
			assertEquals(Duration.ofSeconds(4), animator.time());
			assertEquals(0.5f, animation.get());
		}

		@DisplayName("wraps around at the end of the animation if the animator is repeating")
		@Test
		void wrap() {
			animator.repeat(true);
			animator.frame(new Frame(instant, instant.plusSeconds(12)));
			assertEquals(true, animator.isPlaying());
			assertEquals(Duration.ofSeconds(4), animator.time());
			assertEquals(0.5f, animation.get());
		}

		@DisplayName("stops at the end of the animation if the animator is not repeating")
		@Test
		void end() {
			animator.repeat(false);
			animator.frame(new Frame(instant, instant.plusSeconds(9)));
			assertEquals(false, animator.isPlaying());
			assertEquals(Duration.ofSeconds(8), animator.time());
			assertEquals(1, animation.get());
		}
	}
}
