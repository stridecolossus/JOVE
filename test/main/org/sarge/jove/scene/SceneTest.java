package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;

public class SceneTest {
	private Scene scene;

	private RenderContext ctx;
	private Projection projection;
	private Viewport viewport;
	private Rectangle rect;

	@Before
	public void before() {
		// Create projection
		rect = new Rectangle( 0, 0, 800, 600 );
		projection = mock( Projection.class );
		when( projection.getMatrix( 0.1f, 100f, rect.getDimensions() ) ).thenReturn( new Matrix( 4 ) );

		// Create viewport
		viewport = mock( Viewport.class );

		// Create scene
		scene = new Scene( viewport, rect, projection );

		// Create rendering context
		ctx = mock( RenderContext.class );
	}

	@Test
	public void constructor() {
		assertNotNull( scene.getCamera() );
		assertEquals( rect, scene.getRectangle() );
		assertEquals( projection, scene.getProjection() );
		assertNotNull( scene.getProjectionMatrix() );
		verify( projection ).getMatrix( 0.1f, 100f, rect.getDimensions() );
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
	public void render() {
		// Render scene with a node
		final Node root = mock( Node.class );
		scene.setRoot( root );
		scene.setClearColour( Colour.BLACK );
		scene.render( ctx );

		// Check viewport is initialised
		verify( viewport ).init( rect );
		verify( viewport ).clear( Colour.BLACK );
		verify( viewport ).clear();

		// Check scene is rendered
		verify( ctx ).setScene( scene );
		verify( root ).accept( ctx );
		verify( ctx ).setScene( null );
	}
}
