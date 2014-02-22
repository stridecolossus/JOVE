package org.sarge.jove.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.BufferUtils;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Builder for a {@link AbstractMesh}.
 * TODO - should produce a BufferedMesh rather than storing VBO/IBOs here
 * 		- IndexedMeshBuilder extends MeshBuilder
 * 		- only need MeshLayout when actually constructing mesh?
 * @author Sarge
 */
public class MeshBuilder {
	// Config
	private final MeshLayout layout;

	// Mesh data
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Integer> indices = new ArrayList<>();

	// Buffered data
	private final List<FloatBuffer> vertexBuffers = new ArrayList<>();
	private IntBuffer indexBuffer;

	/**
	 * Constructor.
	 * @param layout Mesh descriptor
	 */
	public MeshBuilder( MeshLayout layout ) {
		Check.notNull( layout );
		this.layout = layout;
	}

	/**
	 * @return Mesh layout for this builder
	 */
	public MeshLayout getLayout() {
		return layout;
	}

	/**
	 * @return Vertex data
	 */
	public List<Vertex> getVertices() {
		return vertices;
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
	public int getVertexCount() {
		if( indices.isEmpty() ) {
			return vertices.size();
		}
		else {
			return indices.size();
		}
	}

	/**
	 * Looks up a vertex, using the indices is supplied.
	 * @param idx Vertex index
	 * @return Indexed vertex
	 */
	public Vertex getIndexedVertex( int idx ) {
		if( indices.isEmpty() ) {
			return vertices.get( idx );
		}
		else {
			return vertices.get( indices.get( idx ) );
		}
	}

	/**
	 * @param start		Start index
	 * @param end		End index
	 * @return Iterator over mesh faces
	 */
	public Iterator<List<Vertex>> getFaceIterator( final int start, final int end ) {
		final Primitive primitive = layout.getPrimitive();
		final int size = primitive.getSize();
		final List<Vertex> array = new ArrayList<>( size );

		return new Iterator<List<Vertex>>() {
			private int idx = start;

			@Override
			public boolean hasNext() {
				return idx < end;
			}

			@Override
			public List<Vertex> next() {
				if( ( idx == start ) || ( primitive.getStride() != 1 ) ) {
					// Load next face
					array.clear();
					for( int n = 0; n < size; ++n ) {
						array.add( getIndexedVertex( idx + n ) );
					}
				}
				else {
					// Append next index
					Collections.rotate( array, -1 );
					array.set( size - 1, getIndexedVertex( idx ) );
				}

				// Move to next face
				idx += primitive.getStride();

				return array;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Adds a vertex.
	 * @param v Vertex
	 */
	public void add( Vertex v ) {
		Check.notNull( v );
		vertices.add( v );
	}

	/**
	 * Adds a list of vertices.
	 * @param list Vertex data
	 */
	public void addVertices( List<Vertex> list ) {
		for( Vertex v : list ) {
			add( v );
		}
	}

	/**
	 * Creates a bounding box from this mesh.
	 * @return Bounding box
	 */
	public BoundingBox getBounds() {
		// TODO - this sucks, iterate over vertices and update() BB
		final Collection<Point> points = new ArrayList<>( vertices.size() );
		for( Vertex v : vertices ) {
			points.add( v.getPosition() );
		}
		return new BoundingBox( points );
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
		if( layout.getPrimitive() != Primitive.TRIANGLE_STRIP ) throw new UnsupportedOperationException(); // TODO - triangles as well?

		// Calc end index
		final int end = start + len;
		if( end > vertices.size() ) throw new IndexOutOfBoundsException( "Invalid vertex index" );

		// Create triangles
		for( int n = start; n < end; ++n ) {
			indices.add( n );
			indices.add( n + len );
		}
	}

	/**
	 * Adds a quad comprised of two triangles.
	 * @param array Quad of vertices
	 */
	public void addQuad( Vertex[] array ) {
		Check.notNull( array );
		if( array.length != 4 ) throw new IllegalArgumentException( "Expected quad array" );

		// Top-left triangle
		add( array[0] );
		add( array[1] );
		add( array[2] );

		// Bottom-right triangle
		switch( layout.getPrimitive() ) {
		case TRIANGLES:
			add( array[2] );
			add( array[1] );
			add( array[3] );
			break;

		case TRIANGLE_STRIP:
			add( array[3] );
			break;

		default:
			throw new IllegalArgumentException( "Invalid primitive for quad: " + layout.getPrimitive() );
		}
	}

	/**
	 * Adds a quad of the given size at the origin of the X-Y plane.
	 * @param size Quad size
	 */
	public void addQuad( Quad quad ) {
		addQuad( quad.getVertices() );
	}

	/**
	 * @return Whether this mesh requires normals
	 */
	public boolean hasNormals() {
		return layout.contains( DefaultBufferDataType.NORMALS );
	}

	/**
	 * Generates <b>all</b> vertex normals in this mesh.
	 */
	public void computeNormals() {
		// Init normals
		for( Vertex v : vertices ) {
			v.setNormal( new Vector() );
		}

		// Compute normals
		computeNormals( 0, getFaceCount() );
	}

	/**
	 * Generates vertex normals based on averaging adjacent vertices.
	 * @param start		Starting index
	 * @param end		End index
	 */
	public void computeNormals( int start, int end ) {
		// Check mesh is ready
		if( !hasNormals() ) throw new IllegalArgumentException( "Normals not specified: " + layout );
		verify();

		// Check valid for this primitive
		final Primitive primitive = layout.getPrimitive();
		if( !primitive.hasNormals() ) throw new IllegalArgumentException( "Invalid primitive for normals: " + primitive );

		// Sum normals at each triangle vertex
		boolean even = true;
		final Iterator<List<Vertex>> itr = getFaceIterator( start, end );
		while( itr.hasNext() ) {
			// Get next triangle
			final List<Vertex> tri = itr.next();

			// Sum normals at each corner
			for( int c = 0; c < 3; ++c ) {
				// Builds vectors to other corners of this triangle
				final Vertex vertex = tri.get( 0 );
				final Point pt = vertex.getPosition();
				final Vector u = tri.get( 1 ).getPosition().subtract( pt );
				final Vector v = tri.get( 2 ).getPosition().subtract( pt );

				// Update vertex normal
				if( even ) {
					vertex.addNormal( u.cross( v ) );
				}
				else {
					vertex.addNormal( v.cross( u ) );
				}

				// Move to next corner of this triangle
				Collections.rotate( tri, 1 );
			}

			// Swap normal direction
			if( primitive == Primitive.TRIANGLE_STRIP ) {
				even = !even;
			}
		}
	}

	/**
	 * Normalizes <b>all</b> mesh normals.
	 */
	public void normalize() {
		for( Vertex v : vertices ) {
			final Vector n = v.getNormal().normalize();
			v.setNormal( n );
		}
	}

	/**
	 * Allocates pre-determined buffer length.
	 * @param len Maximum buffer length
	 */
	public void allocate( int len ) {
		for( BufferLayout b : layout.getBufferLayout() ) {
			final int size = b.getSize() * len;
			final FloatBuffer vbo = BufferUtils.createFloatBuffer( size );
			vertexBuffers.add( vbo );
		}
	}

	/**
	 * Rebuilds all VBOs.
	 */
	public void build() {
		build( 0, vertices.size() );
	}

	/**
	 * Rebuilds VBOs starting at the given vertex.
	 * @param start Starting vertex
	 */
	public void build( int start ) {
		build( start, vertices.size() );
	}

	private void verify() {
		if( !layout.getPrimitive().isValidVertexCount( vertices.size() ) ) throw new IllegalArgumentException( "Invalid number of vertices for primitive" );
	}

	/**
	 * Rebuilds a segment of the VBOs.
	 * @param start		Starting index
	 * @param end		End index
	 */
	public void build( int start, int end ) {
		Check.zeroOrMore( start );
		if( end < start ) throw new IllegalArgumentException( "End must be after start vertex" );
		if( end > vertices.size() ) throw new IllegalArgumentException( "Not enough vertices" );
		verify();

		// Init buffers
		if( vertexBuffers.isEmpty() ) {
			// Create fixed-size vertex buffers
			allocate( vertices.size() );

			// Create index buffer
			if( !indices.isEmpty() ) {
				indexBuffer = BufferUtils.createIntegerBuffer( indices.size() );
			}
		}

		// Populate buffers
		for( int n = 0; n < layout.getBufferLayout().size(); ++n ) {
			// Lookup layout and associated buffer
			final BufferLayout b = layout.getBufferLayout().get( n );
			final FloatBuffer fb = vertexBuffers.get( n );

			// Populate buffer segment
			fb.position( start * b.getSize() );
			for( int v = start; v < end; ++v ) {
				b.append( vertices.get( v ), fb );
			}

			// Prepare buffer for rendering
			fb.rewind();
		}

		// Populate index buffer
		// TODO - should this be bound by start/end? or separate update methods?
		if( indexBuffer != null ) {
			for( int idx : indices ) indexBuffer.put( idx );
			indexBuffer.rewind();
		}
	}

	/**
	 * @return Mesh VBOs
	 */
	public List<FloatBuffer> getVertexBuffers() {
		return vertexBuffers;
	}

	/**
	 * @return Index buffer or <tt>null</tt> if no indices
	 */
	public IntBuffer getIndexBuffer() {
		return indexBuffer;
	}

	/**
	 * @return Number of faces for the drawing primitive of this mesh
	 */
	public int getFaceCount() {
		final int count;
		if( indices.isEmpty() ) {
			count = vertices.size();
		}
		else {
			count = indices.size();
		}

		return layout.getPrimitive().getFaceCount( count );
	}

	/**
	 * Tests whether this mesh has been built and is ready to be uploaded to the hardware.
	 * @return whether this mesh has been built
	 */
	public boolean isReady() {
		return !vertexBuffers.isEmpty();
	}

	/**
	 * Resets this builder.
	 */
	public void reset() {
		vertices.clear();
		indexBuffer = null;
		vertexBuffers.clear();
	}

	@Override
	public String toString() {
		final ToString ts = new ToString( this );
		ts.append( "layout", layout );
		ts.append( "count", getFaceCount() );
		return ts.toString();
	}
}
