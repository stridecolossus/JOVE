package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Storage layout scheme for VBOs.
 * @author Sarge
 */
public class MeshLayout {
	private static MeshLayout DEFAULT_LAYOUT = create( Primitive.TRIANGLE_STRIP, "VN0", false );

	/**
	 * @return Default mesh layout (triangle-strip with normals and texture coordinates in slot zero)
	 */
	public static MeshLayout getDefaultLayout() {
		return DEFAULT_LAYOUT;
	}

	/**
	 * Sets the default mesh layout.
	 * @param layout Default mesh layout
	 */
	public static void setDefaultLayout( MeshLayout layout ) {
		Check.notNull( layout );
		MeshLayout.DEFAULT_LAYOUT = layout;
	}

	/**
	 * Convenience factory method to create a layout from a simple string representation.
	 * <p>
	 * The component data-types are represented by single upper-case characters as follows:
	 * <ul>
	 * <li>V - vertices</li>
	 * <li>N - normals</li>
	 * <li>C - colours</li>
	 * <li>0 - texture coords for slot 0</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>VBOs are interleaved by default if comprised of multiple components.</li>
	 * <li>Multiple VBOs can be defined with space delimiters.</li>
	 * <li>VBOs are static (uploaded once) by default, dynamic VBOs can be specified by a <tt>~</tt> character prefix.</li>
	 * </ul>
	 * <p>
	 * Example: <tt>~V N01</tt>
	 * <p>
	 * In the above, streamed vertices are stored in the first VBO, static normals and texture coordinates for slots 0 and 1 are interleaved in the second VBO.
	 * <p>
	 * @param primitive			Drawing primitive
	 * @param layout			Layout scheme
	 * @param dynamicIndices	Whether the index buffer is dynamic or static
	 * @return Mesh-layout
	 */
	public static MeshLayout create( Primitive primitive, String layout, boolean dynamicIndices ) {
		Check.notNull( primitive );
		Check.notEmpty( layout );

		// Build buffer layout(s)
		final List<BufferLayout> buffers = new ArrayList<>();
		for( String str : layout.split( " " ) ) {
			// Determine access mode
			final AccessMode mode;
			switch( str.charAt( 0 ) ) {
			case '~':
				mode = AccessMode.STREAM;
				break;

			case '+':
				mode = AccessMode.DYNAMIC;
				break;

			default:
				mode = AccessMode.STATIC;
				break;
			}

			// Skip prefix
			if( mode != AccessMode.STATIC ) {
				str = str.substring( 1 );
			}

			// Build list of data-types in this buffer
			final List<BufferDataType> types = new ArrayList<>();
			for( char ch : str.toCharArray() ) {
				types.add( mapType( ch ) );
			}

			// Create buffer layout
			buffers.add( new BufferLayout( types, mode ) );
		}

		// Create mesh layout
		return new MeshLayout( primitive, buffers, dynamicIndices );
	}

	/**
	 * Maps a data-type character to the actual data-type.
	 */
	private static BufferDataType mapType( char ch ) {
		if( Character.isDigit( ch ) ) {
			return TextureBufferDataType.get( ch - '0' );
		}
		else {
			switch( ch ) {
			case 'V' : return DefaultBufferDataType.VERTICES;
			case 'N' : return DefaultBufferDataType.NORMALS;
			case 'C' : return DefaultBufferDataType.COLOURS;
			}
		}

		throw new IllegalArgumentException( "Invalid data type: " + ch );
	}

	private final Primitive primitive;
	private final List<BufferLayout> layout;
	private final boolean dynamicBuffers;
	private final boolean dynamicIndices;
	private final boolean hasNormals;

	/**
	 * Constructor.
	 * @param primitive			Drawing primitive
	 * @param layout 			List of buffer layouts
	 * @param dynamicIndices	Whether the index buffer is dynamic or static
	 * @throws IllegalArgumentException if the layout is invalid
	 */
	public MeshLayout( Primitive primitive, List<BufferLayout> layout, boolean dynamicIndices ) {
		Check.notNull( primitive );

		// Init layout properties
		boolean vertexData = false;
		boolean dynamic = false;
		boolean normals = false;
		final Set<BufferDataType> used = new HashSet<>();
		for( BufferLayout b : layout ) {
			// Check for duplicate buffers
			final List<BufferDataType> types = b.getBufferDataTypes();
			BufferLayout.checkDuplicates( types, used );

			// Check for mandatory vertex data
			if( types.contains( DefaultBufferDataType.VERTICES ) ) vertexData = true;

			// Note whether layout supports normals
			if( types.contains( DefaultBufferDataType.NORMALS ) ) normals = true;

			// Note whether layout is dynamic
			if( b.getAccessMode().isDynamic() ) dynamic = true;
		}

		// Check vertex data was specified
		if( !vertexData ) throw new IllegalArgumentException( "No vertex data specified" );

		// Check normals are logical for this primitive
		if( normals && !primitive.hasNormals() ) throw new IllegalArgumentException( "Primitive does not support normals: " + primitive );

		this.primitive = primitive;
		this.layout = Collections.unmodifiableList( layout );
		this.dynamicBuffers = dynamic;
		this.dynamicIndices = dynamicIndices;
		this.hasNormals = normals;
	}

	public MeshLayout( Primitive primitive, List<BufferLayout> layout ) {
		this( primitive, layout, false );
	}

	/**
	 * @return Drawing primitive
	 */
	public Primitive getPrimitive() {
		return primitive;
	}

	/**
	 * @return List of buffer layouts comprising this mesh layout
	 */
	public List<BufferLayout> getBufferLayout() {
		return layout;
	}

	/**
	 * @return Whether this layout has dynamically updated buffers
	 */
	public boolean isDynamic() {
		return dynamicBuffers;
	}

	/**
	 * @return Whether this layout has a dynamic index buffer
	 */
	public boolean isIndexBufferDynamic() {
		return dynamicIndices;
	}

	/**
	 * Convenience test for normals.
	 * @return Whether this layout supports normals
	 * @see #contains(BufferDataType)
	 */
	public boolean hasNormals() {
		return hasNormals;
	}

	/**
	 * @return Whether this layout contains the given component-type
	 */
	public boolean contains( BufferDataType type ) {
		for( BufferLayout b : layout ) {
			for( BufferDataType t : b.getBufferDataTypes() ) {
				if( t == type ) return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
