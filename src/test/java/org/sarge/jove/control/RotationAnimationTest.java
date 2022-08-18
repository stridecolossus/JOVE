package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

class RotationAnimationTest {
	private RotationAnimation animation;

	@BeforeEach
	void before() {
		animation = new RotationAnimation(Vector.Y);
	}

	@Test
	void constructor() {
		assertNotNull(animation.rotation());
		assertEquals(Vector.Y, animation.rotation().rotation().axis());
		assertEquals(0, animation.rotation().rotation().angle());
	}

	@Test
	void update() {
		final Animator animator = mock(Animator.class);
		when(animator.position()).thenReturn(MathsUtil.HALF);
		animation.update(animator);
		assertEquals(MathsUtil.PI, animation.rotation().rotation().angle());
	}
}
