package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class RotationAnimationTest {
	private RotationAnimation animation;

	@BeforeEach
	void before() {
		animation = new RotationAnimation(Vector.X_AXIS);
	}

	@Test
	void constructor() {
		assertEquals(Rotation.of(Vector.X_AXIS, 0), animation.rotation());
	}

	@Test
	void update() {
		final Animator animator = mock(Animator.class);
		when(animator.position()).thenReturn(MathsUtil.HALF);
		animation.update(animator);
		assertEquals(Rotation.of(Vector.X_AXIS, MathsUtil.PI), animation.rotation());
	}
}
