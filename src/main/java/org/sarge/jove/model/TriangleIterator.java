package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.sarge.lib.util.Check;

/**
 * Iterator over triangles in an indexed mesh.
 * @author Sarge
 */
public class TriangleIterator implements Iterator<Triangle> {
	private final MeshBuilder builder;
	private final int end;
	private final List<Vertex> vertices;
	private final Primitive primitive;

	private final Triangle triangle = new Triangle() {
		@Override
		public Vertex getVertex( int n ) {
			return vertices.get( n );
		}
	};

	private int idx;

	/**
	 * Constructor.
	 * @param builder		Associated indexed mesh
	 * @param start			Starting face index
	 * @param end			End index
	 */
	public TriangleIterator( MeshBuilder builder, int start, int end ) {
		Check.notNull( builder );
		Check.zeroOrMore( start );
		if( end <= start ) throw new IllegalArgumentException( "Start must be before end" );
		if( start >= builder.getFaceCount() ) throw new IllegalArgumentException( "Invalid starting face index" );
		if( end > builder.getFaceCount() ) throw new IllegalArgumentException( "Invalid end face index" );
		if( builder.getLayout().getPrimitive().getSize() != 3 ) throw new IllegalArgumentException( "Not a triangle-mesh" );

		this.builder = builder;
		this.idx = start;
		this.end = end;
		this.primitive = builder.getLayout().getPrimitive();
		this.vertices = new ArrayList<>( 3 );

		// Init vertex array
		for( int n = 0; n < 3; ++n ) {
			vertices.add( null );
		}

		// Init vertex data if using rotation approach
		if( primitive.getStride() == 1 ) {
			populate();
			Collections.rotate( vertices, +1 );
		}
	}

	/**
	 * Constructor.
	 * @param builder Associated indexed mesh
	 */
	public TriangleIterator( MeshBuilder builder ) {
		this( builder, 0, builder.getFaceCount() );
	}

	@Override
	public boolean hasNext() {
		return idx < end;
	}

	/**
	 * Initialises the next face.
	 */
	private void populate() {
		final int start = idx * 3;
		for( int n = 0; n < 3; ++n ) {
			vertices.set( n, builder.getIndexedVertex( start + n ) );
		}
	}

	@Override
	public Triangle next() {
		if( !hasNext() ) throw new NoSuchElementException();

		// Populate next face
		if( primitive.getStride() == 1 ) {
			// Move one step and append next vertex to end of triangle
			final Vertex v = builder.getIndexedVertex( idx + 2 );
			Collections.rotate( vertices, -1 );
			vertices.set( 2, v );
		}
		else {
			// Otherwise re-populate entire triangle
			populate();
		}

		// Move to next triangle
		++idx;

		return triangle;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}


/**
 * @param start		Start index
 * @param end		End index
 * @return Iterator over mesh faces
 *
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
*/
