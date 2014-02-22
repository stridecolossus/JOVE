package org.sarge.jove.common;

import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferUtils;

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
	public void fade() {
		final Colour fade = new Colour( 0.5f, 1, 0, 1 );
		col = col.fade( fade );
		assertFloatEquals( 0.05f, col.getRed() );
		assertFloatEquals( 0.2f, col.getGreen() );
		assertFloatEquals( 0f, col.getBlue() );
		assertFloatEquals( 0.4f, col.getAlpha() );
	}

	@Test
	public void appendFloatBuffer() {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer( 4 );
		col.append( buffer );
		buffer.flip();
		assertFloatEquals( 0.1f, buffer.get() );
		assertFloatEquals( 0.2f, buffer.get() );
		assertFloatEquals( 0.3f, buffer.get() );
		assertFloatEquals( 0.4f, buffer.get() );
	}
}
