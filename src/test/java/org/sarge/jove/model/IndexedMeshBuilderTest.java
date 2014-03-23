package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class IndexedMeshBuilderTest {
	private IndexedMeshBuilder builder;
	private Vertex vertex;

	@Before
	public void before() {
		builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "VN", false ) );
		vertex = createVertex();
	}

	private static Vertex createVertex() {
		final Vertex v = new Vertex( new Point( 1, 2, 3 ) );
		v.setNormal( new Vector( 4, 5, 6 ) );
		v.setTextureCoords( new TextureCoord( 0.7f, 0.8f ) );
		return v;
	}

	@Test
	public void constructor() {
		// Check layout
		assertNotNull( builder.getLayout() );
		assertEquals( -2, builder.getFaceCount() );
		assertEquals( true, builder.hasNormals() );

		// Check empty vertices
		assertNotNull( builder.getVertices() );
		assertEquals( true, builder.getVertices().isEmpty() );
		assertEquals( 0, builder.getVertexCount() );

		// Check empty indices
		assertNotNull( builder.getIndices() );
		assertEquals( 0, builder.getIndices().size() );
		assertEquals( new Integer( 0 ), builder.getIndexSize() );
	}

	@Test
	public void addIndex() {
		builder.add( vertex );
		builder.addIndex( 0 );
		assertEquals( new Integer( 1 ), builder.getIndexSize() );
		assertEquals( 1, builder.getIndices().size() );
		assertEquals( new Integer( 0 ), builder.getIndices().get( 0 ) );
	}

	@Test
	public void addIndexArray() {
		builder.add( vertex );
		builder.add( vertex );
		builder.addIndices( Arrays.asList( 0, 1 ) );
		assertEquals( new Integer( 2 ), builder.getIndexSize() );
		assertEquals( 2, builder.getIndices().size() );
		assertEquals( new Integer( 0 ), builder.getIndices().get( 0 ) );
		assertEquals( new Integer( 1 ), builder.getIndices().get( 1 ) );
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void addIndexInvalidVertexCount() {
		builder.addIndex( 0 );
	}

	@Test
	public void addStrip() {
		for( int n = 0; n < 4; ++n ) {
			builder.add( vertex );
		}
		builder.addStrip( 0, 2 );
		assertEquals( new Integer( 4 ), builder.getIndexSize() );
		assertEquals( 4, builder.getIndices().size() );
		assertEquals( new Integer( 0 ), builder.getIndices().get( 0 ) );
		assertEquals( new Integer( 2 ), builder.getIndices().get( 1 ) );
		assertEquals( new Integer( 1 ), builder.getIndices().get( 2 ) );
		assertEquals( new Integer( 3 ), builder.getIndices().get( 3 ) );
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void addStripInvalidVertexCount() {
		builder.addStrip( 0, 1 );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void addStripInvalidPrimitive() {
		builder.add( vertex );
		builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.TRIANGLE_FAN, "VN0", false ) );
		builder.addStrip( 0, 1 );
	}

	@Test
	public void computeNormals() {
		builder.addQuad( new Quad() );
		builder.addStrip( 0, 2 );
		builder.computeNormals();
		for( int n = 0; n < 4; ++n ) {
			assertEquals( Vector.Z_AXIS.invert(), builder.getVertices().get( n ).getNormal() );
		}
	}

	@Test
	public void build() {
		// Add two triangles
		builder.addQuad( new Quad() );
		builder.addStrip( 0, 2 );
		builder.computeNormals();

		// Construct mesh
		final BufferedMesh mesh = builder.build();
		assertNotNull( mesh );

		// Check index buffer
		final IntBuffer index = mesh.getIndexBuffer();
		assertNotNull( index );
		assertEquals( 4, index.capacity() );
		assertEquals( 0, index.position() );
		assertEquals( 0, index.get() );
		assertEquals( 2, index.get() );
		assertEquals( 1, index.get() );
		assertEquals( 3, index.get() );
	}

	@Test
	public void reset() {
		builder.add( vertex );
		builder.addIndex( 0 );
		builder.reset();
		assertEquals( new Integer( 0 ), builder.getIndexSize() );
		assertEquals( true, builder.getIndices().isEmpty() );
	}
}
