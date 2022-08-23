package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Playable.State;

class BoundAnimatorTest {
	private BoundAnimator animator;
	private Animation animation;

	@BeforeEach
	void before() {
		animation = mock(Animation.class);
		animator = new BoundAnimator(1, animation);
	}

	@Test
	void constructor() {
		assertEquals(false, animator.isPlaying());
		assertEquals(false, animator.isRepeating());
		assertEquals(0, animator.position());
		assertEquals(1, animator.speed());
	}

	@DisplayName("A non-repeating animation stops at the end of the duration")
	@Test
	void finished() throws InterruptedException {
		animator.state(State.PLAY);
		Thread.sleep(50);
		animator.frame();
		assertEquals(false, animator.isPlaying());
		assertEquals(1, animator.position());
	}

	@DisplayName("A repeating animation cycles at the end of the duration")
	@Test
	void repeating() throws InterruptedException {
		animator.repeat(true);
		animator.state(State.PLAY);
		Thread.sleep(50);
		animator.frame();
		assertEquals(true, animator.isPlaying());
	}
}
