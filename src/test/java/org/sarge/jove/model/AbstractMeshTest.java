package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.RenderContext;

public class AbstractMeshTest {
	private static BufferedMesh createMesh() {
		final MeshBuilder builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "V", false ) );
		for( int n = 0; n < 3; ++n ) {
			builder.add( new Vertex( new Point() ) );
		}
		return builder.build();
	}

	private class MockMesh extends AbstractMesh {
		private boolean bound, drawn, enabled, attribute;

		public MockMesh() {
			super( createMesh() );
		}

		@Override
		protected boolean isSupported() {
			return true;
		}

		@Override
		protected int allocateVAO() {
			return 42;
		}

		@Override
		protected void bind( int id ) {
			if( id == 0 ) {
				bound = false;
			}
			else {
				assertEquals( 42, id );
				bound = true;
			}
		}

		@Override
		protected IndexBufferObject createIndexBuffer() {
			return mock( IndexBufferObject.class );
		}

		@Override
		protected VertexBufferObject createVertexBuffer() {
			return mock( VertexBufferObject.class );
		}

		@Override
		protected void deleteVAO( int id ) {
			assertEquals( 42, id );
		}

		@Override
		protected void draw( int size, IndexBufferObject indices ) {
			drawn = true;
		}

		@Override
		protected void enableVertexAttribute( int idx, boolean enable ) {
			assertEquals( 0, idx );
			enabled = enable;
		}

		@Override
		protected void setVertexAttribute( int idx, int size, int stride, int offset ) {
			assertEquals( 0, idx );
			assertEquals( 3, size );
			assertEquals( 12, stride );
			assertEquals( 0, offset );
			attribute = true;
		}

		@Override
		protected void verify() {
			// Ignored
		}
	}

	private MockMesh mesh;

	@Before
	public void before() {
		mesh = new MockMesh();
	}

	@Test
	public void constructor() {
		assertEquals( 42, mesh.getResourceID() );
//		assertEquals( null, mesh.getIndexBuffer() );
//		assertNotNull( mesh.getVertexBuffers() );
//		assertEquals( 1, mesh.getVertexBuffers().size() );
//		assertNotNull( mesh.getVertexBuffers().iterator().next() );
	}

	@Test
	public void render() {
//		final StackEntry entry = mock( StackEntry.class );
//		when( entry.getShader() ).thenReturn( mock( ShaderProgram.class ) );
		final RenderContext ctx = mock( RenderContext.class );
//		when( ctx.getActiveStackEntry() ).thenReturn( entry );
		mesh.render( ctx );
		assertEquals( true, mesh.drawn );
		assertEquals( true, mesh.attribute );
	}

	@Test
	public void bind() {
		mesh.bind( mesh.getResourceID() );
		assertEquals( true, mesh.bound );

		mesh.bind( 0 );
		assertEquals( false, mesh.bound );
	}

	@Test
	public void vertexAttributes() {
		mesh.enableVertexAttribute( 0, true );
		assertEquals( true, mesh.enabled );

		mesh.enableVertexAttribute( 0, false );
		assertEquals( false, mesh.enabled );
	}
}
