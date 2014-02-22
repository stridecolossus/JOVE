package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Appendable;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;

public class TextureBufferDataTypeTest {
	private TextureBufferDataType type;

	@Before
	public void before() {
		type = TextureBufferDataType.get( 3 );
	}

	@Test
	public void constructor() {
		assertEquals( 2, type.getSize() );
		assertEquals( 3, type.getTextureUnit() );
	}

	@Test
	public void getData() {
		// Add some coords to a vertex
		final Vertex vertex = new Vertex( new Point() );
		final TextureCoord coords = new TextureCoord( 0.1f, 0.2f );
		vertex.setTextureCoords( coords );

		// Check coords can be retrieved
		final Appendable data = type.getData( vertex );
		assertEquals( coords, data );
	}
}
