package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;

public class FaceObjectLineParserTest {
	private FaceObjectLineParser parser;
	private ObjectModelData data;

	@Before
	public void before() {
		parser = new FaceObjectLineParser();
		data = mock( ObjectModelData.class );
	}

	@Test
	public void parseVertexOnly() {
		// Add a vertex to the data
		final Point pos = new Point();
		when( data.getVertex( 42 ) ).thenReturn( pos );

		// Parse and check new vertex added to model
		parser.parse( new String[]{ "42" }, data );
		verify( data ).getVertex( 42 );
		verify( data ).add( new Vertex( pos ) );
		verifyNoMoreInteractions( data );
	}

	@Test
	public void parseVertexNormalCoords() {
		// Add vertex, normal and coords to the data
		final Point pos = new Point();
		final TextureCoord coords = new TextureCoord();
		final Vector normal = new Vector();
		final Vertex vertex = new Vertex( pos );
		vertex.setNormal( normal );
		vertex.setTextureCoords( coords );
		when( data.getVertex( 1 ) ).thenReturn( pos );
		when( data.getTextureCoord( 2 ) ).thenReturn( coords );
		when( data.getNormal( 3 ) ).thenReturn( normal );

		// Parse and check new vertex added to model
		parser.parse( new String[]{ "1/2/3" }, data );
		verify( data ).getVertex( 1 );
		verify( data ).getTextureCoord( 2 );
		verify( data ).getNormal( 3 );
		verify( data ).add( vertex );
		verifyNoMoreInteractions( data );
	}

	@Test
	public void parseOptionalTextureCoords() {
		// Add vertex and normal to the data
		final Point pos = new Point();
		final Vector normal = new Vector();
		final Vertex vertex = new Vertex( pos );
		vertex.setNormal( normal );
		when( data.getVertex( 1 ) ).thenReturn( pos );
		when( data.getNormal( 2 ) ).thenReturn( normal );

		// Parse and check new vertex added to model
		parser.parse( new String[]{ "1//2" }, data );
		verify( data ).getVertex( 1 );
		verify( data ).getNormal( 2 );
		verify( data ).add( vertex );
		verifyNoMoreInteractions( data );
	}
}
