package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class TriangleIteratorTest {
	private IndexedMeshBuilder builder;
	private Vertex[] array;

	@Before
	public void before() {
		builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "V", false ) );
		array = new Vertex[ 4 ];
	}

	private void addVertices() {
		for( int n = 0; n < 4; ++n ) {
			final Vertex v = new Vertex( new Point() );
			array[ n ] = v;
			builder.add( v );
		}
	}

	private void addStrip( int start, int end ) {
		for( int n = start; n < end; ++n ) {
			builder.addIndex( n );
		}
	}

	private void checkIterator() {
		final TriangleIterator itr = new TriangleIterator( builder );
		assertEquals( true, itr.hasNext() );

		// Check first face
		final Triangle first = itr.next();
		assertNotNull( first );
		for( int n = 0; n < 3; ++n ) {
			assertEquals( array[ n ], first.getVertex( n ) );
		}

		// Check second face
		assertEquals( true, itr.hasNext() );
		final Triangle second = itr.next();
		assertNotNull( second );
		for( int n = 0; n < 3; ++n ) {
			assertEquals( array[ n + 1 ], second.getVertex( n ) );
		}

		// Check finished
		assertEquals( false, itr.hasNext() );
	}

	@Test
	public void triangleStripIterator() {
		addVertices();
		addStrip( 0, 4 );
		checkIterator();
	}

	@Test
	public void triangleIterator() {
		builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "V", false ) );
		addVertices();
		addStrip( 0, 3 );
		addStrip( 1, 4 );
		checkIterator();
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void invalidPrimitive() {
		builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.LINE_STRIP, "V", false ) );
		addVertices();
		addStrip( 0, 4 );
		new TriangleIterator( builder );
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void invalidStartEnd() {
		addVertices();
		addStrip( 0, 4 );
		new TriangleIterator( builder, 2, 1 );
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void invalidStartIndex() {
		addVertices();
		addStrip( 0, 4 );
		new TriangleIterator( builder, 3, 4 );
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void invalidEndIndex() {
		addVertices();
		addStrip( 0, 4 );
		new TriangleIterator( builder, 1, 3 );
	}
}
