package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.MutableRotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

class RotationAnimationTest {
	private RotationAnimation animation;

	@BeforeEach
	void before() {
		animation = new RotationAnimation(new MutableRotation(Vector.Y));
	}

	@Test
	void constructor() {
		assertNotNull(animation.rotation());
		assertEquals(Vector.Y, animation.rotation().axis());
		assertEquals(0, animation.rotation().angle());
	}

	@Test
	void update() {
		final Animator animator = mock(Animator.class);
		when(animator.position()).thenReturn(MathsUtil.HALF);
		animation.update(animator);
		assertEquals(MathsUtil.PI, animation.rotation().angle());
	}
}
