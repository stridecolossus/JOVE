package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.BufferUtils;

public class BufferLayoutTest {
	@Test
	public void getSize() {
		// Build list of components
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.VERTICES );
		types.add( DefaultBufferDataType.COLOURS );
		types.add( TextureBufferDataType.get( 0 ) );
		types.add( TextureBufferDataType.get( 1 ) );

		// Create layout and check size
		final BufferLayout layout = new BufferLayout( types );
		assertEquals( 3 + 4 + 2 + 2, layout.getSize() );
	}

	@SuppressWarnings("unused")
	@Test( expected = IllegalArgumentException.class )
	public void duplicateDataType() {
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.NORMALS );
		types.add( DefaultBufferDataType.NORMALS );
		new BufferLayout( types );
	}

	@Test
	public void append() {
		// Define a layout
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.NORMALS );
		types.add( TextureBufferDataType.get( 0 ) );
		final BufferLayout layout = new BufferLayout( types );

		// Create a vertex
		final Vertex vertex = new Vertex( new Point() );
		vertex.setNormal( new Vector( 1, 2, 3 ) );
		vertex.setTextureCoords( new TextureCoord( 0.4f, 0.5f ) );

		// Append vertex to buffer
		final FloatBuffer buffer = BufferUtils.createFloatBuffer( 100 );
		layout.append( vertex, buffer );
		buffer.rewind();

		// Check data
		assertEquals( 1, buffer.get(), 0.0001f );
		assertEquals( 2, buffer.get(), 0.0001f );
		assertEquals( 3, buffer.get(), 0.0001f );
		assertEquals( 0.4f, buffer.get(), 0.0001f );
		assertEquals( 0.5f, buffer.get(), 0.0001f );
	}
}
