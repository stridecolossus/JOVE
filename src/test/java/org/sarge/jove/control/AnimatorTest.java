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

	@BeforeEach
	void before() {
		frame = mock(Frame.class);
		animation = mock(Animation.class);
		animator = new Animator(animation) {
			@Override
			public Frame frame() {
				return frame;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(false, animator.isPlaying());
		assertEquals(true, animator.isRepeating());
		assertEquals(1, animator.speed());
		assertEquals(0, animator.elapsed());
		assertEquals(frame, animator.frame());
	}

	@DisplayName("An animation that is not playing...")
	@Nested
	class Stopped {
		@DisplayName("is not updated on frame completion")
		@Test
		void update() {
			animator.update();
			verifyNoMoreInteractions(animation);
		}

		@DisplayName("can be started")
		@Test
		void play() {
			animator.play();
			assertEquals(true, animator.isPlaying());
		}
	}

	@DisplayName("An animation that is playing...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			animator.play();
			assertEquals(true, animator.isPlaying());
		}

		@DisplayName("is updated on frame completion")
		@Test
		void update() {
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(1));
			animator.update();
			assertEquals(true, animator.isPlaying());
			assertEquals(1000, animator.elapsed());
			verify(animation).update(animator);
		}

		@DisplayName("is scaled by the animator speed")
		@Test
		void speed() {
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(2));
			animator.speed(0.5f);
			animator.update();
			assertEquals(0.5f, animator.speed());
			assertEquals(1000, animator.elapsed());
			assertEquals(true, animator.isPlaying());
			verify(animation).update(animator);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			animator.stop();
			animator.update();
			assertEquals(false, animator.isPlaying());
			verifyNoMoreInteractions(animation);
		}
	}
}
