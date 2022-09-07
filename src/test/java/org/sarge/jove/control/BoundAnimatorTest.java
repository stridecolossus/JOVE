package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;

class BoundAnimatorTest {
	private BoundAnimator animator;
	private Animation animation;
	private Frame frame;

	@BeforeEach
	void before() {
		frame = mock(Frame.class);
		animation = mock(Animation.class);
		animator = new BoundAnimator(animation, Duration.ofSeconds(2)) {
			@Override
			public Frame frame() {
				return frame;
			}
		};
		animator.play();
	}

	@Test
	void constructor() {
		assertEquals(Duration.ofSeconds(2), animator.duration());
		assertEquals(true, animator.isRepeating());
		assertEquals(1, animator.speed());
		assertEquals(0, animator.elapsed());
	}

	@DisplayName("cycles the position at the end of the duration if the animator is repeating")
	@Test
	void repeating() {
		when(frame.elapsed()).thenReturn(Duration.ofSeconds(3));
		animator.update();
		assertEquals(0.5f, animator.elapsed());
		assertEquals(true, animator.isPlaying());
		verify(animation).update(animator);
	}

	@DisplayName("stops the animation at the end of the duration if the animator is not repeating")
	@Test
	void finished() {
		when(frame.elapsed()).thenReturn(Duration.ofSeconds(3));
		animator.repeat(false);
		animator.update();
		assertEquals(1, animator.elapsed());
		assertEquals(false, animator.isPlaying());
		verify(animation).update(animator);
	}
}
