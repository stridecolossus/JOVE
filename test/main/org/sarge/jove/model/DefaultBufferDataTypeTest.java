package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class DefaultBufferDataTypeTest {
	private Vertex vertex;

	@Before
	public void before() {
		vertex = new Vertex( new Point( 1, 2, 3 ) );
	}

	@Test
	public void vertices() {
		assertEquals( new Point( 1, 2, 3 ), DefaultBufferDataType.VERTICES.getData( vertex ) );
	}

	@Test
	public void normals() {
		final Vector normal = new Vector();
		vertex.setNormal( normal );
		assertEquals( normal, DefaultBufferDataType.NORMALS.getData( vertex ) );
	}

	@Test
	public void coords() {
		final Colour col = Colour.WHITE;
		vertex.setColour( col );
		assertEquals( Colour.WHITE, DefaultBufferDataType.COLOURS.getData( vertex ) );
	}
}
