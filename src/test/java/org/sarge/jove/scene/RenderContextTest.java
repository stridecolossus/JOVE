package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Matrix;

public class RenderContextTest {
	private RenderContext ctx;
	private RenderingSystem sys;

	@Before
	public void before() {
		sys = mock( RenderingSystem.class );
		ctx = new RenderContext( sys );
	}

	@Test
	public void constructor() {
		assertEquals( 0, ctx.getElapsed() );
		assertFloatEquals( 0, ctx.getFramesPerSecond() );
		assertEquals( sys, ctx.getRenderingSystem() );
		assertEquals( null, ctx.getScene() );
	}

	@Test
	public void setScene() {
		final Scene scene = mock( Scene.class );
		ctx.setScene( scene );
		assertEquals( scene, ctx.getScene() );
	}

	@Test
	public void setModelMatrix() {
		final Matrix m = Matrix.IDENTITY;
		ctx.setModelMatrix( m );
		assertEquals( m, ctx.getModelMatrix() );
	}
}
