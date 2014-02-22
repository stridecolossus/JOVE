package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.scene.Node;
import org.sarge.lib.io.DataSource;

public class ObjectModelDataTest {
	private ObjectModelData data;
	private RenderingSystem sys;

	@Before
	public void before() {
		sys = mock( RenderingSystem.class );
		data = new ObjectModelData( mock( DataSource.class ), sys );
	}

	@Test
	public void constructor() {
		assertNotNull( data.getRootNode() );
		assertTrue( data.getMaterialLibrary().isEmpty() );
	}

	@Test
	public void addMaterial() {
		final Material mat = new MutableMaterial( "mat" );
		data.add( mat );
		assertEquals( mat, data.getMaterialLibrary().get( "mat" ) );
	}

	@Test
	public void startNode() {
		// Start a new node
		data.startNode( "new" );

		// Add a triangle to the model
		for( int n = 0; n < 3; ++n ) {
			final Vertex vertex = new Vertex( new Point() );
			vertex.setNormal( new Vector() );
			vertex.setTextureCoords( new TextureCoord() );
			data.add( vertex );
		}

		// Mock mesh
		final AbstractMesh mesh = mock( AbstractMesh.class );
		when( sys.createMesh( any( MeshBuilder.class ) ) ).thenReturn( mesh );

		// Check model root node
		final Node root = data.getRootNode();
		assertNotNull( root );
		assertEquals( "root", root.getName() );
		assertEquals( 1, root.getChildren().size() );

		// Check node and mesh was built and added to the model
		final Node node = (Node) root.getChildren().get( 0 );
		assertEquals( "new", node.getName() );
		assertEquals( mesh, node.getChildren().get( 0 ) );
	}

	@Test
	public void addVertex() {
		final Point pos = new Point();
		data.add( pos );
		assertEquals( pos, data.getVertex( 1 ) );
	}

	@Test
	public void addNormal() {
		final Vector normal = new Vector();
		data.add( normal );
		assertEquals( normal, data.getNormal( 1 ) );
	}

	@Test
	public void addTextureCoords() {
		final TextureCoord coords = new TextureCoord();
		data.add( coords );
		assertEquals( coords, data.getTextureCoord( 1 ) );
	}

	public void getNegativeIndex() {
		final Point pos = new Point();
		data.add( pos );
		assertEquals( pos, data.getVertex( -1 ) );
	}

	@Test( expected = IndexOutOfBoundsException.class )
	public void getVertexInvalidIndex() {
		data.add( new Point() );
		data.getVertex( 2 );
	}

	@Test( expected = IndexOutOfBoundsException.class )
	public void getVertexZeroIndex() {
		data.getVertex( 0 );
	}
}
