package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MutableMatrix;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.light.Light;
import org.sarge.jove.material.Material;
import org.sarge.jove.scene.Node.Visitor;
import org.sarge.jove.util.MockitoTestCase;

public class NodeTest extends MockitoTestCase {
	private Node node;
	private @Mock RenderContext ctx;
	private @Mock Scene scene;

	@Before
	public void before() {
		node = new Node( "node" );
	}

	@Test
	public void constructor() {
		assertEquals( "node", node.getName() );
		assertEquals( null, node.getParent() );
		assertEquals( null, node.getBoundingVolume() );
		assertEquals( null, node.getMaterial() );
		assertTrue( node.getChildren().isEmpty() );
		assertTrue( node.getLights().isEmpty() );
		assertEquals( Matrix.IDENTITY, node.getLocalTransform() );
		assertEquals( Matrix.IDENTITY, node.getWorldTransform() );
	}

	@Test
	public void addRemoveChildren() {
		// Add a child
		final Node child = new Node( "child" );
		node.add( child );
		assertEquals( 1, node.getChildren().size() );
		assertEquals( child, node.getChildren().iterator().next() );

		// Remove it
		node.remove( child );
		assertTrue( node.getChildren().isEmpty() );
	}

	@Test
	public void getParent() {
		// Add a child node and check now has a parent
		final Node child = new Node( "child" );
		node.add( child );
		assertEquals( node, child.getParent() );

		// Remove it and check now has no parent
		node.remove( child );
		assertEquals( null, child.getParent() );
	}

	@Test( expected = IllegalArgumentException.class )
	public void addNodeAlreadyHasParent() {
		// Add child node
		final Node child = new Node( "child" );
		node.add( child );

		// Try to add it again
		final Node other = new Node( "other" );
		other.add( child );
	}

	@Test( expected = IllegalArgumentException.class )
	public void addNodeSelf() {
		node.add( node );
	}

	@Test( expected = IllegalArgumentException.class )
	public void cyclicDependency() {
		final Node parent = new Node( "parent" );
		final Node child = new Node( "child" );
		parent.add( child );
		child.add( parent );
	}

	@Test
	public void setTransform() {
		// Apply local transform
		final Matrix trans = MutableMatrix.translation( new Vector( 1, 2, 3 ) );
		node.setLocalTransform( trans );
		assertEquals( trans, node.getLocalTransform() );
		assertEquals( trans, node.getWorldTransform() );

		// Check inherits parents transform
		final Node parent = new Node( "parent" );
		parent.add( node );
		final Matrix other = MutableMatrix.scale( 2 );
		parent.setLocalTransform( other );
		assertEquals( other.multiply( trans ), node.getWorldTransform() );

		// Check again to ensure dirty flag is reset
		assertEquals( other.multiply( trans ), node.getWorldTransform() );
	}

	@Test
	public void setBoundingVolume() {
		final BoundingVolume vol = mock( BoundingVolume.class );
		node.setBoundingVolume( vol );
		assertEquals( vol, node.getBoundingVolume() );
	}

//	@Test
//	public void boundingVolume() {
//		// Attach a bounding volume
//		final BoundingVolume vol = mock( BoundingVolume.class );
//		node.setBoundingVolume( vol );
//
//		// Apply some transforms and check volume updated
//		final Point pos = new Point( 4, 5, 6 );
//		node.setPosition( pos );
//		node.setScale( 42f );
//		verify( vol ).setCentre( pos );
//		verify( vol ).scale( 42f );
//	}

	@Test
	public void visitor() {
		// Add a child to be visited
		final Node child = new Node( "child" );
		node.add( child );

		// Visit node
		final List<Node> visited = new ArrayList<>();
		final Visitor visitor = new Visitor() {
			@Override
			public boolean visit( Node n ) {
				visited.add( n );
				return true;
			}
		};
		node.accept( visitor );

		// Check recursed to children
		assertEquals( 2, visited.size() );
		assertEquals( node, visited.get( 0 ) );
		assertEquals( child, visited.get( 1 ) );
	}

	@Test
	public void setMaterial() {
		final Material mat = mock( Material.class );
		node.setMaterial( mat );
		assertEquals( mat, node.getMaterial() );
	}

	@Test
	public void addLight() {
		// Add light
		final Light light = mock( Light.class );
		node.add( light );
		assertEquals( 1, node.getLights().size() );
		assertTrue( node.getLights().contains( light ) );

		// Remove it
		node.remove( light );
		assertTrue( node.getLights().isEmpty() );
	}
}
