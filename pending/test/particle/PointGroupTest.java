package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.AccessMode;
import org.sarge.jove.model.DefaultBufferDataType;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.Scene;

public class PointGroupTest {
	private PointGroup<Vertex> group;
	private RenderContext ctx;
	private RenderingSystem sys;
	private AbstractMesh mesh;

	@Before
	public void before() {
		// Create group
		group = new PointGroup<>( true, AccessMode.STREAM );

		// Mock rendering system
		sys = mock( RenderingSystem.class );
		ctx = mock( RenderContext.class );
		when( ctx.getRenderingSystem() ).thenReturn( sys );
		when( ctx.getModelMatrix() ).thenReturn( Matrix.IDENTITY );

		// Mock scene and camera
		final Scene scene = mock( Scene.class );
		when( scene.getCamera() ).thenReturn( new Camera() );
		when( ctx.getScene() ).thenReturn( scene );

		// Mock point mesh
		mesh = mock( AbstractMesh.class );
		when( sys.createMesh( group.getBuilder() ) ).thenReturn( mesh );
	}

	@Test
	public void meshLayout() {
		// Check has builder
		final MeshBuilder builder = group.getBuilder();
		assertNotNull( builder );

		// Check has colours
		final MeshLayout layout = builder.getLayout();
		assertTrue( layout.contains( DefaultBufferDataType.COLOURS ) );

		// Check is dynamic
		assertTrue( layout.isDynamic() );
	}

	@Test
	public void renderInitialised() {
		// Render for first time and check mesh created
		group.render( ctx );
		verify( sys ).createMesh( group.getBuilder() );
		verify( mesh ).render( ctx );

		// Render again and check not created
		group.render( ctx );
		verifyNoMoreInteractions( sys );
		verify( mesh, times( 2 ) ).render( ctx );
	}

	@Test
	public void renderDistanceOrdering() {
		// Add a couple of points
		final MeshBuilder builder = group.getBuilder();
		final Vertex near = add( builder, -1 );
		final Vertex far = add( builder, -2 );

		// Render and check points ordered by distance
		group.render( ctx );
		assertEquals( far, builder.getVertices().get( 0 ) );
		assertEquals( near, builder.getVertices().get( 1 ) );
	}

	private static Vertex add( MeshBuilder builder, float z ) {
		final Point pos = new Point( 0, 0, z );
		final Vertex v = new Vertex( pos );
		v.setColour( Colour.WHITE );
		builder.add( v );
		return v;
	}
}
