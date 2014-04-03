package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;

public class RenderableNodeTest {
	private RenderableNode node;
	private Renderable renderable;

	@Before
	public void before() {
		renderable = mock( Renderable.class );
		node = new RenderableNode( "node", mock( RenderQueue.class ), renderable );
	}

	@Test
	public void constructor() {
		assertEquals( "node", node.getName() );
		assertEquals( null, node.getParent() );
		assertEquals( node, node.getRoot() );
		assertEquals( Matrix.IDENTITY, node.getWorldMatrix() );
		assertNotNull( node.getRenderQueue() );
	}

	@Test
	public void render() {
		// Render node
		node.apply( null );
		verify( renderable ).render( null );

		// Reset node
		node.reset( null );
		verifyNoMoreInteractions( renderable );
	}
}
