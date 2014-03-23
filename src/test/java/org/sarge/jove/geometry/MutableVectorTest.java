package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MutableVectorTest extends VectorTest {
	@Override
	@Before
	public void before() {
		vec = new MutableVector( new Vector( 1, 2, 3 ) );
	}

	@Test
	public void set() {
		final MutableVector m = new MutableVector();
		m.set( vec );
		assertEquals( vec, m );
	}

	@Test
	public void subtract() {
		final MutableVector m = new MutableVector();
		m.subtract( new Point( 1, 2, 3 ), new Point( 4, 5, 6 ) );
		assertEquals( new Vector( 3, 3, 3 ), m );
	}
}
