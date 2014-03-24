package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class RotationAnimationTest {
	private RotationAnimation anim;
	private Rotation rot;

	@Before
	public void before() {
		rot = new Rotation( Vector.Y_AXIS, 0 );
		anim = new RotationAnimation( 5000, rot );
	}

	@Test
	public void update() {
		anim.update( MathsUtil.PI );
		final Matrix expected = Matrix.rotation( new Rotation( Vector.Y_AXIS, MathsUtil.PI ) );
		assertEquals( expected, rot.toMatrix() );
	}
}
