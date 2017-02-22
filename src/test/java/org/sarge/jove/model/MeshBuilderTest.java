package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.BufferFactory;

public class MeshBuilderTest {
	private MeshBuilder builder;
	private Vertex vertex;

	@Before
	public void before() {
		builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "VN0", false ) );
		vertex = createVertex();
	}

	private static Vertex createVertex() {
		final Vertex v = new Vertex( new Point( 1, 2, 3 ) );
		v.setNormal( new Vector( 4, 5, 6 ) );
		v.setTextureCoords( new TextureCoordinate( 0.7f, 0.8f ) );
		return v;
	}

	@Test
	public void constructor() {
		// Check layout
		assertNotNull( builder.getLayout() );
		assertEquals( -2, builder.getFaceCount() );
		assertEquals( true, builder.hasNormals() );
		assertEquals( null, builder.getIndexSize() );

		// Check empty vertices
		assertNotNull( builder.getVertices() );
		assertEquals( true, builder.getVertices().isEmpty() );
		assertEquals( 0, builder.getVertexCount() );
	}

	@Test
	public void add() {
		builder.add( vertex );
		assertEquals( 1, builder.getVertices().size() );
		assertEquals( vertex, builder.getVertices().get( 0 ) );
	}

	@Test
	public void addList() {
		builder.addVertices( Arrays.asList( vertex, vertex, vertex ) );
		assertEquals( 3, builder.getVertices().size() );
		assertEquals( 3, builder.getVertexCount() );
		for( int n = 0; n < 3; ++n ) {
			assertEquals( vertex, builder.getVertices().get( n ) );
		}
		assertEquals( 1, builder.getFaceCount() );
	}

	@Test
	public void getBounds() {
		builder.add( vertex );
		final BoundingBox bounds = builder.getBounds();
		assertNotNull( bounds );
		assertEquals( new Point( 1, 2, 3 ), bounds.getCentre() );
	}

	@Test
	public void addQuadVertexArray() {
		builder.addQuad( new Vertex[]{ vertex, vertex, vertex, vertex } );
		assertEquals( 4, builder.getVertices().size() );
		assertEquals( 4, builder.getVertexCount() );
		assertEquals( 2, builder.getFaceCount() );
	}

	@Test
	public void addQuad() {
		builder.addQuad( new Quad() );
		assertEquals( 4, builder.getVertices().size() );
		assertEquals( 4, builder.getVertexCount() );
		assertEquals( 2, builder.getFaceCount() );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addQuadInvalidArrayLength() {
		builder.addQuad( new Vertex[]{ vertex } );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addQuadInvalidPrimitive() {
		builder = new MeshBuilder( MeshLayout.create( Primitive.LINE_STRIP, "VN0", false ) );
		builder.addQuad( new Quad() );
	}

	@Test
	public void normalize() {
		builder.add( vertex );
		builder.normalize();
		assertEquals( new Vector( 4, 5, 6 ).normalize(), vertex.getNormal() );
	}

	@Test
	public void isValid() {
		// Check initially not valid mesh
		assertEquals( false, builder.isValid() );

		// Add a vertex and check still not valid mesh
		builder.add( vertex );
		assertEquals( false, builder.isValid() );

		// Complete triangle and check is now valid
		builder.add( vertex );
		builder.add( vertex );
		assertEquals( true, builder.isValid() );
	}

	@Test
	public void computeNormals() {
		builder.addQuad( new Quad() );
		builder.computeNormals();
		for( int n = 0; n < 4; ++n ) {
			assertEquals( Vector.Z_AXIS, builder.getVertices().get( n ).getNormal() );
		}
	}

	@Test
	public void reset() {
		builder.add( vertex );
		builder.reset();
		assertEquals( true, builder.getVertices().isEmpty() );
		assertEquals( 0, builder.getVertexCount() );
	}

	private void verifyMesh( BufferedMesh mesh, TextureCoordinate coord ) {
		final FloatBuffer expected = BufferFactory.createFloatBuffer( 3 * ( 3 + 3 + 2 ) );
		for( int n = 0; n < 3; ++n ) {
			vertex.getPosition().append( expected );
			vertex.getNormal().append( expected );
			coord.append( expected );
		}
		expected.flip();
		assertEquals( expected, mesh.getVertexBuffers()[ 0 ] );
	}

	@Test
	public void build() {
		// Add triangle
		for( int n = 0; n < 3; ++n ) {
			builder.add( vertex );
		}

		// Build mesh
		final BufferedMesh mesh = builder.build();
		assertNotNull( mesh );
		assertEquals( builder.getLayout(), mesh.getLayout() );

		// Check vertex buffer created
		assertNotNull( mesh.getVertexBuffers() );
		assertEquals( 1, mesh.getVertexBuffers().length );
		assertEquals( null, mesh.getIndexBuffer() );

		// Check generated buffer
		verifyMesh( mesh, vertex.getTextureCoords() );
	}

	@Test(expected=IllegalArgumentException.class)
	public void buildInvalidVertexCount() {
		builder.add( vertex );
		builder.build();
	}

	@Test
	public void update() {
		// Create mesh
		for( int n = 0; n < 3; ++n ) {
			builder.add( vertex );
		}
		final BufferedMesh mesh = builder.build();

		// Fiddle different texture coords
		final TextureCoordinate coords = new TextureCoordinate( 0.5f, 0.5f );
		vertex.setTextureCoords( coords );

		// Update mesh and check coordinates
		builder.update( mesh );
		verifyMesh( mesh, coords );
	}

	@Test
	public void updateRange() {
		// Create mesh
		for( int n = 0; n < 3; ++n ) {
			builder.add( vertex );
		}
		final BufferedMesh mesh = builder.build();

		// Fiddle different texture coords
		final TextureCoordinate coords = new TextureCoordinate( 0.5f, 0.5f );
		vertex.setTextureCoords( coords );

		// Update mesh and check coordinates
		for( int n = 0; n < 3; ++n ) {
			builder.update( mesh, n, n + 1 );
		}
		verifyMesh( mesh, coords );
	}
}
