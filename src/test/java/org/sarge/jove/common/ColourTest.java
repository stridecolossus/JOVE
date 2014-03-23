package org.sarge.jove.common;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;

public class ColourTest {
	private Colour col;

	@Before
	public void before() {
		col = new Colour( 0.1f, 0.2f, 0.3f, 0.4f );
	}

	@Test
	public void constructor() {
		assertFloatEquals( 0.1f, col.getRed() );
		assertFloatEquals( 0.2f, col.getGreen() );
		assertFloatEquals( 0.3f, col.getBlue() );
		assertFloatEquals( 0.4f, col.getAlpha() );
	}

	@Test
	public void appendFloatBuffer() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( 4 );
		col.append( buffer );
		buffer.flip();
		assertFloatEquals( 0.1f, buffer.get() );
		assertFloatEquals( 0.2f, buffer.get() );
		assertFloatEquals( 0.3f, buffer.get() );
		assertFloatEquals( 0.4f, buffer.get() );
	}

	@Test
	public void equals() {
		final Colour other = new Colour( 0.1f, 0.2f, 0.3f, 0.4f );
		assertEquals( true, col.equals( col ) );
		assertEquals( true, col.equals( other ) );
		assertEquals( true, other.equals( col ) );
		assertEquals( false, col.equals( Colour.BLACK ) );
		assertEquals( false, col.equals( null ) );
	}
}
