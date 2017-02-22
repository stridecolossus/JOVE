package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MatrixBuilder;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;
import org.sarge.jove.scene.NodeGroup.Flag;

public class SceneNodeTest {
	private SceneNode node;

	@Before
	public void before() {
		node = new SceneNode( "node" );
	}

	@Test
	public void constructor() {
		assertEquals( "node", node.getName() );
		assertEquals( null, node.getParent() );
		assertEquals( node, node.getRoot() );
		assertEquals( BoundingVolume.NULL, node.getBoundingVolume() );
		assertEquals( null, node.getMaterial() );
		assertNotNull( node.getChildren() );
		assertTrue( node.getChildren().isEmpty() );
		assertEquals( Matrix.IDENTITY, node.getTransform() );
		assertEquals( Matrix.IDENTITY, node.getWorldMatrix() );
		assertEquals( RenderQueue.Default.OPAQUE, node.getRenderQueue() );
	}

	@Test
	public void setTransform() {
		// Set local transform
		final Matrix trans = MatrixBuilder.translation( new Vector( 1, 2, 3 ) );
		node.setTransform( trans );
		assertEquals( trans, node.getTransform() );
		assertEquals( true, node.isFlagged( Flag.TRANSFORM ) );

		// Get world transform and check no longer dirty
		assertEquals( trans, node.getWorldMatrix() );
		assertEquals( false, node.isFlagged( Flag.TRANSFORM ) );
	}

	@Test
	public void getWorldMatrix() {
		// Attach to parent
		final SceneNode parent = new SceneNode( "parent" );
		node.setParent( parent );

		// Set transform of parent
		final Matrix parentTrans = MatrixBuilder.scale( 2 );
		parent.setTransform( parentTrans );

		// Get world transform and check inherits from parent
		assertEquals( parentTrans, node.getWorldMatrix() );

		// Set local transform and check matrices are combined
		final Matrix trans = MatrixBuilder.translation( new Vector( 1, 2, 3 ) );
		node.setTransform( trans );
		assertEquals( parentTrans.multiply( trans ), node.getWorldMatrix() );
	}

	@Test
	public void setBoundingVolume() {
		final BoundingVolume vol = mock( BoundingVolume.class );
		node.setBoundingVolume( vol );
		assertEquals( vol, node.getBoundingVolume() );
		assertEquals( true, node.isFlagged( Flag.BOUNDING_VOLUME ) );
		// TODO - check propagates upwards
	}

	@Test
	public void setMaterial() {
		final Material mat = mock( Material.class );
		node.setMaterial( mat );
		assertEquals( mat, node.getMaterial() );
	}

	@Test
	public void render() {
		// TODO
	}
}
