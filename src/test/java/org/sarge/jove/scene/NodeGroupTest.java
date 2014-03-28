package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.Node.Visitor;
import org.sarge.jove.scene.NodeGroup.Flag;

public class NodeGroupTest {
	private NodeGroup group;

	private static NodeGroup create() {
		return new NodeGroup( "node", mock( RenderQueue.class ) ) {
			@Override
			public void apply( RenderContext ctx ) {
			}

			@Override
			public void reset( RenderContext ctx ) {
			}
		};
	}

	static void checkEmpty( NodeGroup group ) {
		for( Flag f : Flag.values() ) {
			assertEquals( false, group.isFlagged( f ) );
		}
	}

	@Before
	public void before() {
		group = create();
	}

	@Test
	public void constructor() {
		assertEquals( "node", group.getName() );
		assertEquals( null, group.getParent() );
		assertEquals( group, group.getRoot() );
		assertNotNull( group.getChildren() );
		assertTrue( group.getChildren().isEmpty() );
		assertEquals( Matrix.IDENTITY, group.getWorldMatrix() );
		assertEquals( false, group.isDirtyTransform() );
		assertNotNull( group.getRenderQueue() );
		checkEmpty( group );
	}

	@Test
	public void setParent() {
		// Attach to parent
		final NodeGroup parent = create();
		group.setParent( parent );
		assertEquals( parent, group.getParent() );
		assertEquals( 1, parent.getChildren().size() );
		assertEquals( group, parent.getChildren().get( 0 ) );
		assertEquals( true, parent.isFlagged( Flag.GRAPH ) );
		checkEmpty( group );

		// Remove from scene-graph
		group.setParent( null );
		assertEquals( null, group.getParent() );
		assertEquals( 0, parent.getChildren().size() );
		assertEquals( true, parent.isFlagged( Flag.GRAPH ) );
		checkEmpty( group );
	}

	@Test( expected=IllegalArgumentException.class )
	public void setParentAlreadyAdded() {
		// Add child node
		final NodeGroup parent = create();
		group.setParent( parent );

		// Try to add it again
		final NodeGroup other = create();
		group.setParent( other );
	}

	@Test( expected=IllegalArgumentException.class )
	public void setParentSelf() {
		group.setParent( group );
	}

	@Test( expected=IllegalArgumentException.class )
	public void setParentAncestor() {
		final NodeGroup parent = create();
		group.setParent( parent );
		parent.setParent( group );
	}

	@Test
	public void setFlag() {
		final Flag flag = Flag.TRANSFORM;
		group.set( flag );
		assertEquals( true, group.isFlagged( flag ) );
		group.clear( flag );
		assertEquals( false, group.isFlagged( flag ) );
	}

	@Test
	public void propagate() {
		// Create a scene-graph three deep
		final NodeGroup grandparent = create();
		final NodeGroup parent = create();
		parent.setParent( grandparent );
		group.setParent( parent );

		// Propagate from parent
		parent.propagate( Flag.BOUNDING_VOLUME );
		assertEquals( true, grandparent.isFlagged( Flag.BOUNDING_VOLUME ) );
		assertEquals( true, parent.isFlagged( Flag.BOUNDING_VOLUME ) );
		checkEmpty( group );

		// Propagate from bottom and check all set
		group.propagate( Flag.GRAPH );
		assertEquals( true, grandparent.isFlagged( Flag.GRAPH ) );
		assertEquals( true, parent.isFlagged( Flag.GRAPH ) );
		assertEquals( true, group.isFlagged( Flag.GRAPH ) );
	}

	@Test
	public void accept() {
		// Add a child to be visited
		final NodeGroup child = create();
		child.setParent( group );

		// Create a scene visitor
		final Visitor visitor = mock( Visitor.class );
		when( visitor.visit( group ) ).thenReturn( true );

		// Visit scene-graph and check recurses
		group.accept( visitor );
		verify( visitor ).visit( group );
		verify( visitor ).visit( child );
	}
}
