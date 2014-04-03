package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.Node.Visitor;
import org.sarge.jove.scene.NodeGroup.Flag;

public class NodeGroupTest {
	private NodeGroup group;
	private Node node;

	private static class MockNodeGroup extends NodeGroup {
		public MockNodeGroup() {
			super( "group", RenderQueue.Default.OPAQUE );
		}

		@Override
		public void apply( RenderContext ctx ) {
			// Does nowt
		}

		@Override
		public void reset( RenderContext ctx ) {
			// Does nowt
		}
	}

	private void check( Flag... flags ) {
		final Collection<Flag> c = Arrays.asList( flags );
		for( Flag f : Flag.values() ) {
			assertEquals( c.contains( f ), group.isFlagged( f ) );
		}
	}

	@Before
	public void before() {
		group = new MockNodeGroup();
		node = mock( Node.class );
	}

	@Test
	public void constructor() {
		assertEquals( "group", group.getName() );
		assertEquals( null, group.getParent() );
		assertEquals( group, group.getRoot() );
		assertNotNull( group.getChildren() );
		assertTrue( group.getChildren().isEmpty() );
		assertEquals( Matrix.IDENTITY, group.getWorldMatrix() );
		assertEquals( false, group.isDirtyTransform() );
		assertNotNull( group.getRenderQueue() );
		check();
	}

	@Test
	public void add() {
		// Attach to parent
		group.add( node );
		assertEquals( 1, group.getChildren().size() );
		assertEquals( node, group.getChildren().get( 0 ) );
		check( Flag.GRAPH );

		// Remove from scene-graph
		group.remove( node );
		assertEquals( 0, group.getChildren().size() );
		assertEquals( true, group.isFlagged( Flag.GRAPH ) );
		check( Flag.GRAPH );
	}

	@Test( expected=IllegalArgumentException.class )
	public void addAlreadyHasParent() {
		group.add( node );
		group.add( node );
	}

	@Test( expected=IllegalArgumentException.class )
	public void addSelf() {
		group.add( group );
	}

	@Test( expected=IllegalArgumentException.class )
	public void addCyclic() {
		final NodeGroup other = new MockNodeGroup();
		other.add( group );
		group.add( other );
	}

	@Test
	public void setFlag() {
		// Set flag
		final Flag flag = Flag.TRANSFORM;
		group.set( flag );
		check( flag );

		// Clear flag
		group.clear( flag );
		check();
	}

	@Test
	public void propagate() {
		// Create a scene-graph
		final NodeGroup parent = new MockNodeGroup();
		group.setParent( parent );

		// Propagate and check passed up
		group.propagate( Flag.BOUNDING_VOLUME );
		check( Flag.BOUNDING_VOLUME );
		assertEquals( true, parent.isFlagged( Flag.BOUNDING_VOLUME ) );
	}

	@Test
	public void accept() {
		// Add a child to be visited
		group.add( node );

		// Create a scene visitor
		final Visitor visitor = mock( Visitor.class );
		when( visitor.visit( group ) ).thenReturn( true );

		// Visit scene-graph and check recurses
		group.accept( visitor );
		verify( visitor ).visit( group );
		verify( node ).accept( visitor );
	}
}
