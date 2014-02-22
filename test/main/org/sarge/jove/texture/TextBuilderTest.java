package org.sarge.jove.texture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;

public class TextBuilderTest {
	private TextBuilder builder;
	private TextureFont font;

	@Before
	public void before() {
		font = new DefaultTextureFont( 16, 7, 8 );
		builder = new TextBuilder( font );
	}

	@Test
	public void constructor() {
		assertNotNull( builder.getMeshBuilder() );
		assertFloatEquals( 0, builder.getX() );
		assertFloatEquals( 0, builder.getY() );
	}

	@Test
	public void append() {
		builder.append( "x" );
		assertEquals( 6, builder.getMeshBuilder().getVertices().size() );
		assertEquals( 6, builder.getMeshBuilder().getVertexCount() );
		assertEquals( 2, builder.getMeshBuilder().getFaceCount() );
		assertFloatEquals( font.getWidth( 'x' ), builder.getX() );
	}

	@Test
	public void appendNewline() {
		builder.append( "\n" );
		// TODO - check degenerates
		assertFloatEquals( 0, builder.getX() );
		assertFloatEquals( font.getHeight(), builder.getY() );
	}

	@Test
	public void newline() {
		builder.append( "x" );
		builder.newline();
		assertFloatEquals( 0, builder.getX() );
		assertFloatEquals( font.getHeight(), builder.getY() );
	}

	@Test
	public void clear() {
		builder.append( "some text" );
		builder.clear();
		// TODO - check none
		assertFloatEquals( 0, builder.getX() );
		assertFloatEquals( 0, builder.getY() );
	}

	@Test
	public void setWidth() {
		builder.setWidth( 7 );
		builder.append( "ab" );
		assertFloatEquals( font.getWidth( 'b' ), builder.getX() );
		assertFloatEquals( font.getHeight(), builder.getY() );
	}

	@Test
	public void setColour() {
		final Colour col = new Colour( 0, 1, 0, 1 );
		builder.setColour( col );
		builder.append( "x" );
		// TODO - test vertices colour
	}
}
