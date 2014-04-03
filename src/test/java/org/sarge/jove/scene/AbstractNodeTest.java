package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;

public class AbstractNodeTest {
	private AbstractNode node;
	private RenderQueue queue;

	@Before
	public void before() {
		queue = mock( RenderQueue.class );
		node = new AbstractNode( "node", queue ) {
			@Override
			public void accept( Visitor visitor ) {
				// Does nowt
			}

			@Override
			public void apply( RenderContext ctx ) {
				// Does nowt
			}

			@Override
			public void reset( RenderContext ctx ) {
				// Does nowt
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals( "node", node.getName() );
		assertEquals( null, node.getParent() );
		assertEquals( node, node.getRoot() );
		assertEquals( Matrix.IDENTITY, node.getWorldMatrix() );
		assertEquals( queue, node.getRenderQueue() );
	}

	@Test
	public void setParent() {
		final NodeGroup parent = mock( NodeGroup.class );
		node.setParent( parent );
		assertEquals( parent, node.getParent() );
		assertEquals( parent, node.getRoot() );
		verify( parent ).add( node );
	}

	@Test
	public void moveParent() {
		// Add to parent
		final NodeGroup parent = mock( NodeGroup.class );
		node.setParent( parent );

		// Move to another
		final NodeGroup other = mock( NodeGroup.class );
		node.setParent( other );

		// Check moved
		verify( parent ).remove( node );
		verify( other ).add( node );
		assertEquals( other, node.getParent() );
	}

	@Test
	public void clearParent() {
		final NodeGroup parent = mock( NodeGroup.class );
		node.setParent( parent );
		node.setParent( null );
		assertEquals( null, node.getParent() );
		assertEquals( node, node.getRoot() );
		verify( parent ).add( node );
		verify( parent ).remove( node );
	}

	@Test
	public void getWorldMatrix() {
		final NodeGroup parent = mock( NodeGroup.class );
		final Matrix m = mock( Matrix.class );
		when( parent.getWorldMatrix() ).thenReturn( m );
		node.setParent( parent );
		assertEquals( m, node.getWorldMatrix() );
	}

	@Test
	public void setRenderQueue() {
		final RenderQueue other = mock( RenderQueue.class );
		node.setRenderQueue( other );
		assertEquals( other, node.getRenderQueue() );
	}
}
