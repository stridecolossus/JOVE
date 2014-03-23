package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class BoundingBoxTest {
	private BoundingBox box;

	@Before
	public void before() {
		box = new BoundingBox( new Point( 1, 2, 3 ), new Point( 3, 4, 5 ) );
	}

	@Test
	public void constructor() {
		assertEquals( new Point( 2, 3, 4 ), box.getCentre() );
		assertEquals( new Point( 1, 1, 1 ), box.getExtents() );
	}

	@Test
	public void contains() {
		assertTrue( box.contains( new Point( 1, 2, 3 ) ) );
		assertTrue( box.contains( new Point( 3, 4, 5 ) ) );
		assertFalse( box.contains( new Point( 0, 0, 0 ) ) );
	}

	@Test
	public void pointsConstructor() {
		final Point[] points = {
			new Point( 1, 2, 3 ),
			new Point( 4, 5, 6 ),
			new Point( 7, 8, 9 ),
		};
		box = new BoundingBox( Arrays.asList( points ) );
		assertEquals( new Point( 4, 5, 6 ), box.getCentre() );
		assertEquals( new Point( 3, 3, 3 ), box.getExtents() );
	}
}
