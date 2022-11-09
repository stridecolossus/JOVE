package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Axis;
import org.sarge.jove.util.Trigonometric;

class RotationAnimationTest {
	private RotationAnimation animation;

	@BeforeEach
	void before() {
		animation = new RotationAnimation(Axis.Y);
	}

	@Test
	void constructor() {
		assertNotNull(animation.rotation());
		assertEquals(Axis.Y, animation.rotation().axis());
		assertEquals(0, animation.rotation().angle());
	}

	@Test
	void update() {
		final Animator animator = mock(Animator.class);
		when(animator.elapsed()).thenReturn(0.5f);
		animation.update(animator);
		assertEquals(Trigonometric.PI, animation.rotation().angle());
	}
}
