package org.sarge.jove.model;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Bufferable;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Defines the layout of an interleaved VBO.
 * @author Sarge
 */
public class BufferLayout {
	private final List<BufferDataType> types;
	private final AccessMode mode;
	private final int size;

	/**
	 * Constructor.
	 * @param types		List of the data-types that comprise this scheme element
	 * @param mode		Access mode
	 */
	public BufferLayout( List<BufferDataType> types, AccessMode mode ) {
		// Verify components
		Check.notEmpty( types );
		checkDuplicates( types, new HashSet<BufferDataType>() );
		this.types = Collections.unmodifiableList( types );
		this.mode = mode;

		// Compute component size
		int total = 0;
		for( BufferDataType t : types ) {
			total += t.getSize();
		}
		this.size = total;
	}

	/**
	 * Convenience constructor for {@link AccessMode#STATIC} data.
	 * @param types Data-types in this buffer
	 */
	public BufferLayout( List<BufferDataType> types ) {
		this( types, AccessMode.STATIC );
	}

	/**
	 * @return Data-types that comprise this VBO scheme
	 */
	public List<BufferDataType> getBufferDataTypes() {
		return types;
	}

	/**
	 * @return Buffer access mode
	 */
	public AccessMode getAccessMode() {
		return mode;
	}

	/**
	 * @return Total size of each vertex under this buffer scheme (number of floats per vertex)
	 * @see BufferDataType#getSize()
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Checks for duplicate data-types.
	 * @param types		Data-types being checked
	 * @param used		Existing data-types that have been used
	 */
	static void checkDuplicates( List<BufferDataType> types, Set<BufferDataType> used ) {
		for( BufferDataType t : types ) {
			if( used.contains( t ) ) throw new IllegalArgumentException( "Duplicate data-type: " + t );
			used.add( t );
		}
	}

	/**
	 * Appends vertex data to the given buffer according to this layout.
	 * @param v			Vertex data
	 * @param buffer	Vertex buffer
	 */
	void append( Vertex v, FloatBuffer buffer ) {
		for( BufferDataType t : types ) {
			final Bufferable data = t.getData( v );
			if( data == null ) throw new IllegalArgumentException( "Missing vertex data: data-type=" + t + " vertex=" + v );
			data.append( buffer );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
