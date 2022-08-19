package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.sarge.jove.control.Playable.State.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.control.Playable.State;
import org.sarge.jove.util.MathsUtil;

class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private Frame frame;

	@BeforeEach
	void before() {
		frame = new Frame();
		animation = mock(Animation.class);
		animator = new Animator(2000, animation);
	}

	@Test
	void constructor() {
		assertEquals(false, animator.isPlaying());
		assertEquals(2000, animator.duration());
		assertEquals(1, animator.speed());
		assertEquals(STOP, animator.state());
		assertEquals(false, animator.isPlaying());
		assertEquals(false, animator.isRepeating());
	}

	@DisplayName("A animation that is not playing...")
	@Nested
	class Stopped {
		@DisplayName("is not updated on frame completion")
		@Test
		void update() {
			frame.end();
			animator.completed(frame);
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
		void update() {
			frame.end(Duration.ofSeconds(1));
			animator.completed(frame);
			verify(animation).update(animator);
			assertEquals(1000, animator.time());
			assertEquals(MathsUtil.HALF, animator.position());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			animator.state(State.STOP);
			frame.end(Duration.ofSeconds(1));
			animator.completed(frame);
			assertEquals(false, animator.isPlaying());
			verifyNoMoreInteractions(animation);
		}
	}

	@DisplayName("An animation that has finished...")
	@Nested
	class Finished {
		@BeforeEach
		void before() {
			animator.state(State.PLAY);
			frame.end(Duration.ofSeconds(3));
			animator.completed(frame);
		}

		@DisplayName("has a position at the end of the animation")
		@Test
		void finished() {
			assertEquals(false, animator.isPlaying());
			assertEquals(2000, animator.time());
			assertEquals(1, animator.position());
		}

		@DisplayName("can be restarted")
		@Test
		void restart() {
			animator.state(State.PLAY);
			assertEquals(true, animator.isPlaying());
		}
	}

	@DisplayName("A repeating animation cycles the animation position")
	@Test
	void repeat() {
		frame.end(Duration.ofSeconds(3));
		animator.repeat(true);
		animator.state(State.PLAY);
		animator.completed(frame);
		assertEquals(true, animator.isPlaying());
		assertEquals(1000, animator.time());
		assertEquals(MathsUtil.HALF, animator.position());
	}

	@DisplayName("An animation can be run at different speeds")
	@Test
	void speed() {
		frame.end(Duration.ofSeconds(2));
		animator.speed(MathsUtil.HALF);
		animator.state(State.PLAY);
		animator.completed(frame);
		assertEquals(true, animator.isPlaying());
		assertEquals(1000, animator.time());
		assertEquals(MathsUtil.HALF, animator.position());
	}
}
