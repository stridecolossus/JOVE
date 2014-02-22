package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.BufferUtils;

public class MeshLayoutTest {
	@Test
	public void append() {
		// Define a buffer layout for vertices
		final BufferLayout one = new BufferLayout( Arrays.asList( new BufferDataType[]{ DefaultBufferDataType.VERTICES } ) );

		// Define another buffer layout
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.COLOURS );
		types.add( TextureBufferDataType.get( 0 ) );
		final BufferLayout two = new BufferLayout( types );

		// Create layout
		final MeshLayout layout = new MeshLayout( Primitive.TRIANGLES, Arrays.asList( new BufferLayout[]{ one, two } ) );
		assertEquals( false, layout.contains( DefaultBufferDataType.NORMALS ) );

		// Add vertex
		final Vertex vertex = new Vertex( new Point( 7, 8, 9 ) );
		vertex.setColour( Colour.WHITE );
		vertex.setTextureCoords( new TextureCoord( 0.4f, 0.5f ) );
		final FloatBuffer buffer = BufferUtils.createFloatBuffer( 100 );
		one.append( vertex, buffer );
		two.append( vertex, buffer );
		buffer.rewind();

		// Check data
		assertFloatEquals( 7, buffer.get() );
		assertFloatEquals( 8, buffer.get() );
		assertFloatEquals( 9, buffer.get() );
		assertFloatEquals( 1, buffer.get() );
		assertFloatEquals( 1, buffer.get() );
		assertFloatEquals( 1, buffer.get() );
		assertFloatEquals( 1, buffer.get() );
		assertFloatEquals( 0.4f, buffer.get() );
		assertFloatEquals( 0.5f, buffer.get() );
	}

	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void duplicateDataType() {
		// Define component layout
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.COLOURS );

		// Create two buffer layouts with same component
		final BufferLayout one = new BufferLayout( types );
		final BufferLayout two = new BufferLayout( types );

		// Verify duplicate is rejected
		new MeshLayout( Primitive.TRIANGLES, Arrays.asList( new BufferLayout[]{ one, two } ) );
	}

	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void noVerticesSpecified() {
		final BufferLayout layout = new BufferLayout( Arrays.asList( new BufferDataType[]{ DefaultBufferDataType.NORMALS } ) );
		new MeshLayout( Primitive.TRIANGLES, Collections.singletonList( layout ) );
	}

	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void invalidNormals() {
		// Define layout with normals
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.VERTICES );
		types.add( DefaultBufferDataType.NORMALS );

		// Create invalid layout
		final BufferLayout layout = new BufferLayout( types );
		new MeshLayout( Primitive.POINTS, Collections.singletonList( layout ) );
	}
}
