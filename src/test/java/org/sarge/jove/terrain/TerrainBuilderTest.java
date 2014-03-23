package org.sarge.jove.terrain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.IndexedMeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

@Ignore
public class TerrainBuilderTest {
	private TerrainBuilder builder;

	@Before
	public void before() {
		final Weighting w = new Weighting( 0, 1 );
		final Weighting[] weights = new Weighting[]{ w, w, w };
		builder = new TerrainBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "VNC0", false ), weights );
	}

	@Test
	public void build() {
		// Create a height-map with enough rows to ensure we need degenerate triangles
		final HeightMap map = new MutableHeightMap( 3, 3 );

		// Build terrain
		final IndexedMeshBuilder mesh = (IndexedMeshBuilder) builder.build( map );
		assertNotNull( mesh );

		// Verify mesh
		assertEquals( "Expected 3x3 grid", 9, mesh.getVertices().size() );
		assertEquals( "Expected 2 rows plus 4 degenerates per row", 14, mesh.getIndices().size() );
		assertEquals( "Expected 2 rows plus degenerates", 8 + 4, mesh.getFaceCount() );

		// Verify normals and texture coords
		for( Vertex v : mesh.getVertices() ) {
			assertEquals( Vector.Y_AXIS, v.getNormal() );
			assertNotNull( v.getTextureCoords() );
		}
	}
}
