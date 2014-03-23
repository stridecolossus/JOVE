package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.MathsUtil;

public class CompoundTransformTest {
	private CompoundTransform trans;

	@Before
	public void before() {
		trans = new CompoundTransform();
	}

	@Test
	public void constructor() {
		assertEquals( false, trans.isDirty() );
		assertEquals( Matrix.IDENTITY, trans.toMatrix() );
	}

	@Test
	public void add() {
		// Add a transform and check dirty
		final Transform rot = new Rotation( Vector.Y_AXIS, MathsUtil.HALF_PI );
		trans.add( rot );
		assertEquals( true, trans.isDirty() );

		// Query transform and check no longer dirty
		assertEquals( rot.toMatrix(), trans.toMatrix() );
		assertEquals( false, trans.isDirty() );
	}
}
