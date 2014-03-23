package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class BufferedMeshTest {
	private MeshLayout layout;

	@Before
	public void before() {
		// Create a buffer layout
		final BufferLayout buffer = mock( BufferLayout.class );
		when( buffer.getSize() ).thenReturn( 3 );

		// Create mesh layout
		layout = mock( MeshLayout.class );
		when( layout.getBufferLayout() ).thenReturn( Arrays.asList( buffer, buffer ) );
	}

	@Test
	public void constructor() {
		final BufferedMesh mesh = new BufferedMesh( layout, 4, null );
		assertEquals( layout, mesh.getLayout() );
		assertNotNull( mesh.getVertexBuffers() );
		assertEquals( 2, mesh.getVertexBuffers().length );
		assertEquals( 3 * 4, mesh.getVertexBuffers()[ 0 ].capacity() );
		assertEquals( 3 * 4, mesh.getVertexBuffers()[ 1 ].capacity() );
		assertEquals( null, mesh.getIndexBuffer() );
		assertEquals( 4, mesh.getDrawLength() );
	}

	@Test
	public void constructorIndexedMesh() {
		final BufferedMesh mesh = new BufferedMesh( layout, 4, 7 );
		assertNotNull( mesh.getIndexBuffer() );
		assertEquals( 7, mesh.getIndexBuffer().capacity() );
		assertEquals( 7, mesh.getDrawLength() );
	}
}
