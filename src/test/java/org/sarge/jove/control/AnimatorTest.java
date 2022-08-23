package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.control.Playable.State.PLAY;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Playable.State;

class AnimatorTest {
	private Animator animator;
	private Animation animation;

	@BeforeEach
	void before() {
		animation = mock(Animation.class);
		animator = new Animator(animation);
	}

	@Test
	void constructor() {
		assertEquals(false, animator.isPlaying());
		assertEquals(false, animator.isRepeating());
		assertEquals(0, animator.position());
		assertEquals(1, animator.speed());
	}

	@DisplayName("A animation that is not playing...")
	@Nested
	class Stopped {
		@DisplayName("is not updated on frame completion")
		@Test
		void update() {
			animator.frame();
			verifyNoMoreInteractions(animation);
		}

		@DisplayName("can be started")
		@Test
		void play() {
			animator.state(PLAY);
			assertEquals(true, animator.isPlaying());
		}
	}

	@DisplayName("A playing animation...")
	@Nested
	class Playing {
		@BeforeEach
		void before() {
			animator.state(State.PLAY);
			assertEquals(true, animator.isPlaying());
		}

		@DisplayName("is updated on frame completion")
		@Test
		void update() throws InterruptedException {
			Thread.sleep(50);
			animator.frame();
			verify(animation).update(animator);
			assertTrue(animator.position() > 0);
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			animator.state(State.STOP);
			animator.frame();
			assertEquals(false, animator.isPlaying());
			verifyNoMoreInteractions(animation);
		}
	}

//	@DisplayName("An animation can be run at different speeds")
//	@Test
//	void speed() {
//		frame.end(Duration.ofSeconds(2));
//		animator.speed(MathsUtil.HALF);
//		animator.state(State.PLAY);
//		animator.completed(frame);
//		assertEquals(true, animator.isPlaying());
//		assertEquals(1000, animator.time());
//		assertEquals(MathsUtil.HALF, animator.position());
//	}
}
