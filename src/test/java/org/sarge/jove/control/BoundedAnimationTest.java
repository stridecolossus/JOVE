package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtil;

public class BoundedAnimationTest {
	private BoundedAnimation animation;
	private Animator animator;
	private float updated;

	@BeforeEach
	void before() {
		animator = mock(Animator.class);

		animation = new BoundedAnimation(Duration.ofSeconds(1)) {
			@Override
			protected void update(float pos) {
				updated = pos;
			}
		};
	}

	@Test
	void update() {
		when(animator.elapsed()).thenReturn(500L);
		assertEquals(false, animation.update(animator));
		assertEquals(MathsUtil.HALF, updated);
	}

	@Test
	void cycle() {
		when(animator.elapsed()).thenReturn(1500L);
		assertEquals(false, animation.update(animator));
		assertEquals(MathsUtil.HALF, updated);
	}

	@Test
	void finished() {
		when(animator.elapsed()).thenReturn(1500L);
		animation.repeat(false);
		assertEquals(true, animation.update(animator));
		assertEquals(1, updated);
	}
}
