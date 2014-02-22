package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class MeshBuilderTest {
	private MeshBuilder builder;

	@Before
	public void before() {
		builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "VN0", false ) );
	}

	private Vertex add() {
		final Vertex v = new Vertex( new Point( 1, 2, 3 ) );
		v.setNormal( new Vector( 4, 5, 6 ) );
		v.setTextureCoords( new TextureCoord( 0.7f, 0.8f ) );
		builder.add( v );
		return v;
	}

	@Test
	public void constructor() {
		// Check layout
		assertNotNull( builder.getLayout() );
		assertEquals( -2, builder.getFaceCount() );

		// Check empty vertices
		assertNotNull( builder.getVertices() );
		assertEquals( true, builder.getVertices().isEmpty() );

		// Check empty indices
		assertNotNull( builder.getIndices() );
		assertEquals( true, builder.getIndices().isEmpty() );

		// Check empty VBOs
		assertNotNull( builder.getVertexBuffers() );
		assertEquals( true, builder.getVertexBuffers().isEmpty() );

		// Check no index buffer
		assertEquals( null, builder.getIndexBuffer() );
	}

	@Test
	public void addVertex() {
		final Vertex v = add();
		assertNotNull( builder.getVertices() );
		assertEquals( 1, builder.getVertices().size() );
		assertEquals( v, builder.getVertices().iterator().next() );
	}

	@Test
	public void getBounds() {
		add();
		final BoundingBox bounds = builder.getBounds();
		assertNotNull( bounds );
		assertEquals( new Point( 1, 2, 3 ), bounds.getMin() );
		assertEquals( new Point( 1, 2, 3 ), bounds.getMax() );
	}

	@Test
	public void addIndex() {
		add();
		builder.addIndex( 0 );
		assertNotNull( builder.getIndices() );
		assertEquals( 1, builder.getIndices().size() );
		assertEquals( new Integer( 0 ), builder.getIndices().iterator().next() );
	}

	@Test( expected = IndexOutOfBoundsException.class )
	public void addIndexOutOfBounds() {
		builder.addIndex( 0 );
	}

	@Test
	public void addStrip() {
		// Add some vertices
		for( int n = 0; n < 4; ++n ) {
			add();
		}

		// Create 2x2 grid
		builder.addStrip( 0, 2 );
		assertEquals( 4, builder.getIndices().size() );
		assertEquals( 2, builder.getFaceCount() );

		// Check triangle winding
		assertEquals( new Integer( 0 ), builder.getIndices().get( 0 ) );
		assertEquals( new Integer( 2 ), builder.getIndices().get( 1 ) );
		assertEquals( new Integer( 1 ), builder.getIndices().get( 2 ) );
		assertEquals( new Integer( 3 ), builder.getIndices().get( 3 ) );
	}

	@Test
	public void computeNormals() {
		// Make a triangle
		builder.add( new Vertex( new Point( 0, 0, 0 ) ) );
		builder.add( new Vertex( new Point( 1, 0, 0 ) ) );
		builder.add( new Vertex( new Point( 0, 1, 0 ) ) );

		// Compute normals and check generated for each corner of the triangle
		builder.computeNormals();
		final Vector z = new Vector( 0, 0, 1 );
		for( Vertex v : builder.getVertices() ) {
			assertEquals( z, v.getNormal() );
		}
	}

	@Test
	public void build() {
		// Create a triangle
		for( int n = 0; n < 3; ++n ) {
			add();
			builder.addIndex( n );
		}

		// Check has one triangle
		assertEquals( 1, builder.getFaceCount() );

		// Build VBOs
		builder.build();

		// Verify index buffer
		final IntBuffer indexBuffer = builder.getIndexBuffer();
		assertNotNull( indexBuffer );
		assertEquals( 3, indexBuffer.limit() );
		for( int n = 0; n < 3; ++n ) {
			assertEquals( n, indexBuffer.get() );
		}

		// Verify vertex buffer
		assertNotNull( builder.getVertexBuffers() );
		assertEquals( 1, builder.getVertexBuffers().size() );
		final FloatBuffer vbo = builder.getVertexBuffers().iterator().next();
		assertNotNull( vbo );
		assertEquals( 3 * ( 3 + 3 + 2 ), vbo.limit() );
		for( int n = 0; n < 3; ++n ) {
			// Check vertex
			assertFloatEquals( 1, vbo.get() );
			assertFloatEquals( 2, vbo.get() );
			assertFloatEquals( 3, vbo.get() );

			// Check normal
			assertFloatEquals( 4, vbo.get() );
			assertFloatEquals( 5, vbo.get() );
			assertFloatEquals( 6, vbo.get() );

			// Check texture coords
			assertFloatEquals( 0.7f, vbo.get() );
			assertFloatEquals( 0.8f, vbo.get() );
		}
	}

//	@Test( expected = IllegalArgumentException.class )
//	public void buildNoVertices() {
//		builder.build();
//	}

	@Test( expected = IllegalArgumentException.class )
	public void buildInsufficientIndices() {
		add();
		add();
		add();
		builder.addIndex( 1 );
		builder.build();
	}

	@Test( expected = IllegalArgumentException.class )
	public void buildIndicesMismatch() {
		builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "VN0", false ) );
		add();
		add();
		add();
		add();
		for( int n = 0; n < 4; ++n ) builder.addIndex( n );
		builder.build();
	}
}
