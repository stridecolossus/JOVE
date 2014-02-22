package org.sarge.jove.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.Scene;

public class MaterialPropertyTest {
	private RenderContext ctx;

	@Before
	public void before() {
		final RenderingSystem sys = mock( RenderingSystem.class );
		ctx = new RenderContext( sys );
	}

	@Test
	public void time() {
		assertEquals( ctx.getTime(), MaterialProperty.TIME.getValue( ctx ) );
		assertEquals( ctx.getElapsed(), MaterialProperty.ELAPSED_TIME.getValue( ctx ) );
	}

	@Test
	public void matrices() {
		// Mock projection matrix
		final Scene scene = mock( Scene.class );
		when( scene.getProjectionMatrix() ).thenReturn( Matrix.IDENTITY );

		// Mock camera matrix
		final Camera cam = mock( Camera.class );
		when( scene.getCamera() ).thenReturn( cam );
		when( cam.getViewMatrix() ).thenReturn( Matrix.IDENTITY );

		// Mock context
		ctx = mock( RenderContext.class );
		when( ctx.getScene() ).thenReturn( scene );
		when( ctx.getModelMatrix() ).thenReturn( Matrix.IDENTITY );

//		// Mock current model-matrix
//		final Node node = mock( Node.class );
//		when( node.getWorldTransform() ).thenReturn( Matrix.IDENTITY );
//		final StackEntry entry = mock( StackEntry.class );
//		when( entry.getNode() ).thenReturn( node );
//		when( ctx.getActiveStackEntry() ).thenReturn( entry );
//		when( node.getWorldTransform() ).thenReturn( Matrix.IDENTITY );

		// Check PVM matrices
		assertNotNull( MaterialProperty.PROJECTION_MATRIX.getValue( ctx ) );
		assertNotNull( MaterialProperty.VIEW_MATRIX.getValue( ctx ) );
		assertNotNull( MaterialProperty.MODEL_MATRIX.getValue( ctx ) );

		// Check compound matrices
		assertNotNull( MaterialProperty.MODELVIEW_MATRIX.getValue( ctx ) );
		assertNotNull( MaterialProperty.PROJECTION_MATRIX.getValue( ctx ) );
	}
}
