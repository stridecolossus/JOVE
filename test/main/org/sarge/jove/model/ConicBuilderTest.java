package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class ConicBuilderTest {
	private ConicBuilder builder;
	
	@Before
	public void before() {
		builder = new ConicBuilder( MeshLayout.create( "VN0" ), new UnitCircle( 8 ) );
	}
	
	@Test
	public void addRing() {
		// Verify vertices match circle
		final MeshBuilder mesh = new MeshBuilder( Primitive.TRIANGLE_STRIP, MeshLayout.create( "VN0" ) );
		builder.addRing( 1, 2, mesh );
		assertNotNull( mesh );
		assertEquals( 8, mesh.getVertices().size() );
		
		// Verify vertices
		final Point centre = new Point( 0, 2, 0 );
		for( Vertex v : mesh.getVertices() ) {
			System.out.println(v.getPosition()+" "+v.getNormal());
			assertEquals( "Expected vertical position to match radius", 2, v.getPosition().y, 0.0001f );
			assertEquals( "Expected vertex to be on circle radius", 2 * 2, v.getPosition().distanceSquared( centre ), 0.0001f );
			// TODO - assertEquals( v.getPosition(), v.getNormal() );
		}
	}
	
	@Test
	public void createSphere() {
		final MeshBuilder mesh = builder.createSphere( 5, 8 );
		assertNotNull( mesh );
		assertEquals( 64, mesh.getVertices().size() );
	}
	
	@Test
	public void createCone() {
		final MeshBuilder mesh = builder.createCone( 5, 2 );
		assertNotNull( mesh );
		assertEquals( 16, mesh.getVertices().size() );
	}
	
	@Test
	public void createCylinder() {
		final MeshBuilder mesh = builder.createCone( 5, 2 );
		assertNotNull( mesh );
		assertEquals( 16, mesh.getVertices().size() );
	}
}
