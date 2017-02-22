package org.sarge.jove.animation;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.animation.Animator.Animation;
import org.sarge.jove.geometry.MutableRotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class RotationAnimationTest {
	private Animation anim;
	private MutableRotation rot;

	@Before
	public void before() {
		rot = new MutableRotation(Vector.Y_AXIS, 0);
		anim = rot.animation();
	}

	@Test
	public void update() {
		anim.update(2500L, 5000L);
		assertFloatEquals(MathsUtil.PI, rot.getAngle());
	}
}
