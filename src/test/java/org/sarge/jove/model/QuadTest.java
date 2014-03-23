package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;

public class QuadTest {
	private Quad quad;

	@Before
	public void before() {
		quad = new Quad( new Point( 1, 2, 3 ), 4, 6 );
	}

	@Test
	public void constructor() {
		final Vertex[] vertices = quad.getVertices();
		assertNotNull( vertices );
		assertEquals( 4, vertices.length );
		assertEquals( new Point( -1, +5, 3 ), vertices[ 0 ].getPosition() );
		assertEquals( new Point( -1, -1, 3 ), vertices[ 1 ].getPosition() );
		assertEquals( new Point( +3, +5, 3 ), vertices[ 2 ].getPosition() );
		assertEquals( new Point( +3, -1, 3 ), vertices[ 3 ].getPosition() );
	}

	@Test
	public void topLeftConstructor() {
		quad = new Quad( new Point( 1, 2, 3 ), 4, 6, false );
		final Vertex[] vertices = quad.getVertices();
		assertNotNull( vertices );
		assertEquals( 4, vertices.length );
		assertEquals( new Point( +1, +2, 3 ), vertices[ 0 ].getPosition() );
		assertEquals( new Point( +1, -4, 3 ), vertices[ 1 ].getPosition() );
		assertEquals( new Point( +5, +2, 3 ), vertices[ 2 ].getPosition() );
		assertEquals( new Point( +5, -4, 3 ), vertices[ 3 ].getPosition() );
	}

	@Test
	public void setColour() {
		quad.setColour( Colour.WHITE );
		for( Vertex v : quad.getVertices() ) {
			assertEquals( Colour.WHITE, v.getColour() );
		}
	}

	@Test
	public void setDefaultTextureCoords() {
		quad.setDefaultTextureCoords();
		assertEquals( Quad.TOP_LEFT, quad.getVertices()[ 0 ].getTextureCoords() );
		assertEquals( Quad.BOTTOM_LEFT, quad.getVertices()[ 1 ].getTextureCoords() );
		assertEquals( Quad.TOP_RIGHT, quad.getVertices()[ 2 ].getTextureCoords() );
		assertEquals( Quad.BOTTOM_RIGHT, quad.getVertices()[ 3 ].getTextureCoords() );
	}
}
