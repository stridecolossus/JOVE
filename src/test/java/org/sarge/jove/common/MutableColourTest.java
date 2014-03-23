package org.sarge.jove.common;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class MutableColourTest {
	private MutableColour col;

	@Before
	public void before() {
		col = new MutableColour( 0.1f, 0.2f, 0.3f, 0.4f );
	}

	@Test
	public void constructor() {
		assertFloatEquals( 0.1f, col.getRed() );
		assertFloatEquals( 0.2f, col.getGreen() );
		assertFloatEquals( 0.3f, col.getBlue() );
		assertFloatEquals( 0.4f, col.getAlpha() );
	}

	@Test
	public void set() {
		final Colour other = new Colour( 0.9f, 0.8f, 0.7f, 0.6f );
		col.set( other );
		assertEquals( other, col );
	}

	@Test
	public void scale() {
		col.scale( 2 );
		assertFloatEquals( 0.2f, col.getRed() );
		assertFloatEquals( 0.4f, col.getGreen() );
		assertFloatEquals( 0.6f, col.getBlue() );
		assertFloatEquals( 0.4f, col.getAlpha() );
	}
}
