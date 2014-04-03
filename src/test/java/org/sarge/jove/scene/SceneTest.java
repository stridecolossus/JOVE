package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;

public class SceneTest {
	private Scene scene;
	private Viewport viewport;
	private Projection projection;
	private RenderManager mgr;

	@Before
	public void before() {
		projection = mock( Projection.class );
		viewport = mock( Viewport.class );
		mgr = mock( RenderManager.class );
		scene = new Scene( viewport, new Rectangle( 0, 0, 640, 480 ), projection, mgr );
	}

	@Test
	public void constructor() {
		assertEquals( new Rectangle( 0, 0, 640, 480 ), scene.getRectangle() );
		assertNotNull( scene.getCamera() );
		assertNotNull( scene.getDistanceComparator() );
		assertEquals( projection, scene.getProjection() );
	}

	@Test
	public void setRectangle() {
		final Rectangle rect = new Rectangle( 1, 2, 3, 4 );
		scene.setRectangle( rect );
		assertEquals( rect, scene.getRectangle() );
		scene.getProjectionMatrix();
		verify( projection ).getMatrix( 0.1f, 1000f, new Dimensions( 3, 4 ) );
	}

	@Test
	public void setFrustumPlanes() {
		scene.setNearPlane( 1 );
		scene.setFarPlane( 2 );
		scene.getProjectionMatrix();
		verify( projection ).getMatrix( 1, 2, new Dimensions( 640, 480 ) );
	}

	@Test
	public void setProjection() {
		final Projection proj = mock( Projection.class );
		scene.setProjection( proj );
		assertEquals( proj, scene.getProjection() );
		scene.getProjectionMatrix();
		verify( proj ).getMatrix( 0.1f, 1000f, new Dimensions( 640, 480 ) );
	}

	/*
	@Test
	public void contains() {
		proj = new PerspectiveProjection();

		// Test inside frustum
		assertTrue( scene.contains( new Point( 0, 0, -10f ) ) );

		// Test points on near/far planes
		assertTrue( scene.contains( new Point( 0, 0, -0.01f ) ) );
		assertTrue( scene.contains( new Point( 0, 0, -95f ) ) );

		// Test outside frustum
		assertFalse( scene.contains( new Point( 0, 0, 5f ) ) );
		assertFalse( scene.contains( new Point( 0, 0, -100f ) ) );
	}
	*/

	@Test
	public void getDistanceComparator() {
		final NodeGroup node = mock( NodeGroup.class );
		when( node.getWorldMatrix() ).thenReturn( Matrix.IDENTITY );
		assertEquals( 0, scene.getDistanceComparator().compare( node, node ) );
	}

	@Test
	public void render() {
		// Init scene
		final Node node = mock( Node.class );
		final RenderContext ctx = mock( RenderContext.class );
		scene.setClearColour( Colour.WHITE );
		scene.setRoot( node );

		// Render scene and check viewport
		scene.render( ctx );
		verify( viewport ).init( new Rectangle( 0, 0, 640, 480 ) );
		verify( viewport ).clear( Colour.WHITE );

		// Check manager
		verify( node ).accept( mgr );
		verify( mgr ).sort( scene.getDistanceComparator() );
		verify( mgr ).render( ctx );
	}

	// TODO
	// - unproject
	// - pick
	// - contains
}
