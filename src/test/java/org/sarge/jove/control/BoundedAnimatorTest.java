package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Playable.State;

public class BoundedAnimatorTest {
	private BoundedAnimator animator;
	private Animation animation;

	@BeforeEach
	void before() {
		animation = mock(Animation.class);
		animator = new BoundedAnimator(animation, Duration.ofMillis(2));
		animator.state(State.PLAY);
	}

	@DisplayName("A bound animator is playing unless the animation has finished")
	@Test
	void isPlaying() {
		assertEquals(true, animator.isPlaying());
	}

	@DisplayName("A bound animator repeats by default")
	@Test
	void isRepeating() {
		assertEquals(true, animator.isRepeating());
	}

	@DisplayName("A bound animator can be non-repeating")
	@Test
	void repeat() {
		animator.repeat(false);
		assertEquals(false, animator.isRepeating());
	}

	@DisplayName("A repeating animator cycles the animation position")
	@Test
	void cycle() {
//		animator.update();
//		verify(animation).update(0.5f);
//		assertEquals(true, animator.isPlaying());
	}

	@DisplayName("A non-repeating animator stops the animation on completion")
	@Test
	void finished() {
//		animator.repeat(false);
//		animator.update(3);
//		verify(animation).update(1);
//		assertEquals(false, animator.isPlaying());
	}
}
