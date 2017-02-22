package org.sarge.jove.model;

import java.nio.FloatBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.BufferFactory;
import org.sarge.lib.util.Check;

/**
 * Buffered mesh data.
 * @author Sarge
 */
public class BufferedMesh {
//	private final MeshLayout layout;
//	private final FloatBuffer[] vertexBuffers;
//	private final int len;
//	private final IntBuffer indexBuffer;

	/**
	 * Constructor.
	 * @param layout		Mesh descriptor
	 * @param len			Number of vertices
	 * @param indexSize		Index buffer size or <b>null</b> if none
	 */
	public BufferedMesh( MeshLayout layout, int len, Integer indexSize ) {
		Check.notNull( layout );
		Check.oneOrMore( len );

		this.layout = layout;

		// Allocate buffers array
		final int num = layout.getBufferLayout().size();
		vertexBuffers = new FloatBuffer[ num ];

		// Create buffers
		for( int n = 0; n < num; ++n ) {
			final BufferLayout b = layout.getBufferLayout().get( n );
			final int size = b.getSize() * len;
			vertexBuffers[ n ] = BufferFactory.createFloatBuffer( size );
			// TODO - assumes 4 x floats
		}

		// Create index buffer
		if( indexSize == null ) {
			indexBuffer = null;
			this.len = len;
		}
		else {
			indexBuffer = BufferFactory.createIntegerBuffer( indexSize );
			this.len = indexSize;
		}
	}

	/**
	 * @return Mesh layout for this builder
	 */
	public MeshLayout getLayout() {
		return layout;
	}

	/**
	 * @return Number of vertices
	 */
	public int getDrawLength() {
		return len;
	}

	/**
	 * @return Vertex buffers array
	 */
	public FloatBuffer[] getVertexBuffers() {
		return vertexBuffers;
	}

//	/**
//	 * @return Index buffer or <tt>null</tt> if not indexed
//	 */
//	public IntBuffer getIndexBuffer() {
//		return indexBuffer;
//	}
	
	public static class Builder {
		private FloatBuffer buffer;
		
		public BufferedMeshBuilder(int size) {
			this.buffer = BufferFactory.createFloatBuffer(size);
		}
		
		public void add(float... values) {
			for(float f : values) {
				buffer.put(f);
			}
		}
		
		public void add(Bufferable... objects) {
			for(Bufferable obj : objects) {
				obj.append(buffer);
			}
		}
		
		public FloatBuffer build() {
			final FloatBuffer result = buffer;
			buffer = BufferFactory.createFloatBuffer(buffer.limit());
			return result;
		}
	}
}
