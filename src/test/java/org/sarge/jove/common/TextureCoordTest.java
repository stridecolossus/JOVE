package org.sarge.jove.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;

public class TextureCoordTest {
	private TextureCoord coords;

	@Before
	public void before() {
		coords = new TextureCoord( 0.1f, 0.2f );
	}

	@Test
	public void append() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( 2 );
		coords.append( buffer );
		buffer.rewind();
		assertFloatEquals( 0.1f, buffer.get() );
		assertFloatEquals( 0.2f, buffer.get() );
	}

	@Test
	public void equals() {
		assertTrue( coords.equals( coords ) );
		assertTrue( coords.equals( new TextureCoord( 0.1f, 0.2f ) ) );
		assertFalse( coords.equals( null ) );
		assertFalse( coords.equals( new TextureCoord( 0.3f, 0.4f ) ) );
	}
}
