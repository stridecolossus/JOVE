package org.sarge.jove.model;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.util.Check;

/**
 * Builder for an indexed mesh.
 * @author Sarge
 */
public class IndexedMeshBuilder extends MeshBuilder {
	private final List<Integer> indices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param layout Mesh descriptor
	 */
	public IndexedMeshBuilder( MeshLayout layout ) {
		super( layout );
	}

	/**
	 * @return Indices
	 */
	public List<Integer> getIndices() {
		return indices;
	}

	/**
	 * @return Number of rendered vertices
	 */
	@Override
	public int getVertexCount() {
		return indices.size();
	}

	@Override
	public Vertex getIndexedVertex( int idx ) {
		return vertices.get( indices.get( idx ) );
	}

	/**
	 * Adds an index.
	 * @param idx Vertex index 0..n
	 * @throws IndexOutOfBoundsException if the index is larger than the number of vertices
	 */
	public void addIndex( int idx ) {
		Check.zeroOrMore( idx );
		if( idx >= vertices.size() ) throw new IndexOutOfBoundsException( "Invalid vertex index" );
		indices.add( idx );
	}

	/**
	 * Adds a list of indices.
	 * @param list Indices
	 */
	public void addIndices( List<Integer> list ) {
		for( int idx : list ) {
			addIndex( idx );
		}
	}

	/**
	 * Adds a strip to the mesh.
	 * The strip indices are <tt>start, start + len, start + 1, start + len + 1,<tt> etc.
	 * @param start		Starting vertex
	 * @param len		Length of strip
	 */
	public void addStrip( int start, int len ) {
		Check.zeroOrMore( start );
		Check.oneOrMore( len );
		if( layout.getPrimitive() != Primitive.TRIANGLE_STRIP ) throw new UnsupportedOperationException( "Invalid primitive for strips" );

		// Calc end index
		final int end = start + len;
		if( end > vertices.size() ) throw new IndexOutOfBoundsException( "Invalid vertex index" );

		// Create triangles
		for( int n = start; n < end; ++n ) {
			indices.add( n );
			indices.add( n + len );
		}
	}

	@Override
	protected Integer getIndexSize() {
		return indices.size();
	}

	/**
	 * Updates vertex data in the given range.
	 * @param mesh		Buffered mesh to update
	 * @param start		Start index
	 * @param end		End index
	 */
	@Override
	public void update( BufferedMesh mesh, int start, int end ) {
		// Update vertices
		super.update( mesh, start, end );

		// Populate index buffer
		// TODO - should this be bound by start/end? or separate update methods?
		final IntBuffer buffer = mesh.getIndexBuffer();
		for( int idx : indices ) {
			buffer.put( idx );
		}
		buffer.rewind();
	}

	/**
	 * Resets this builder.
	 */
	@Override
	public void reset() {
		super.reset();
		indices.clear();
	}
}
