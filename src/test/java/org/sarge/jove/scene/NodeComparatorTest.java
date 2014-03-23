package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;

public class NodeComparatorTest {
	private NodeComparator comparator;

	@Before
	public void before() {
		comparator = new NodeComparator( true );
	}

	@Test
	public void compare() {
		// Set eye-camera
		comparator.setEyePosition( new Point( 2, 1, 0 ) );

		// Create node with distance of 5
		final Node nearest = new Node( "nearest" );
		nearest.setLocalTransform( Matrix.translation( new Point( 5, 5, 0 ) ) );

		// Create another at 6
		final Node furthest = new Node( "furthest" );
		nearest.setLocalTransform( Matrix.translation( new Point( 2, 7, 0 ) ) );

		// Check comparator
		assertEquals( true, comparator.compare( nearest, furthest ) < 0 );
		assertEquals( true, comparator.compare( furthest, nearest ) > 0 );
	}
}
