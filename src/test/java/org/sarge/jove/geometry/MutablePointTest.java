package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MutablePointTest extends PointTest {
	@Override
	@Before
	public void before() {
		pt = new MutablePoint( 1, 2, 3 );
	}

	@Test
	public void set() {
		final MutablePoint m = new MutablePoint();
		m.set( pt );
		assertEquals( pt, m );
	}
}
