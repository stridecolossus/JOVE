package org.sarge.jove.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RectangleTest {
	private Rectangle rect;

	@Before
	public void before() {
		rect = new Rectangle( 1, 2, 3, 4 );
	}

	@Test
	public void constructor() {
		assertEquals( 1, rect.getX() );
		assertEquals( 2, rect.getY() );
		assertEquals( 3, rect.getWidth() );
		assertEquals( 4, rect.getHeight() );
		assertEquals( new Dimensions( 3, 4 ), rect.getDimensions() );
	}

	@Test
	public void equals() {
		assertEquals( rect, rect );
		assertEquals( true, rect.equals( rect ) );
		assertEquals( true, rect.equals( new Rectangle( 1, 2, 3, 4 ) ) );
		assertEquals( false, rect.equals( new Rectangle( 1, 2, 3, 999 ) ) );
		assertEquals( false, rect.equals( null ) );
	}
}
