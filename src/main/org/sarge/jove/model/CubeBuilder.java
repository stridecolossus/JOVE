package org.sarge.jove.model;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mesh builder for a cube.
 * @author Sarge
 */
public class CubeBuilder {
	private static final float[][] vertices = new float[][] {
		// Front
		{ -1, +1, +1 },
		{ -1, -1, +1 },
		{ +1, +1, +1 },
		{ +1, -1, +1 },

		// Back
		{ +1, +1, -1 },
		{ +1, -1, -1 },
		{ -1, +1, -1 },
		{ -1, -1, -1 },
	};

	// Note faces ordered in texture-cube fashion
	private static final int[] indices = new int[] {
		2, 3, 4, 5,			// Right
		6, 7, 0, 1,			// Left
		6, 0, 4, 2,			// Top
		1, 7, 3, 5,			// Bottom
		4, 5, 6, 7,			// Back
		0, 1, 2, 3,			// Front
	};

	// Coloured-cube
	private static final Colour[] colours = new Colour[] {
		new Colour( 1, 0, 0, 1 ),
		new Colour( 1, 1, 0, 1 ),
		Colour.WHITE,
		Colour.BLACK,
		new Colour( 0, 1, 0, 1 ),
		new Colour( 0, 0, 1, 1 ),
	};

	/**
	 * Convenience builder for a cube using the default mesh layout.
	 * @param size Cube size
	 * @return Cube mesh
	 */
	public static MeshBuilder build( float size ) {
		final CubeBuilder cube = new CubeBuilder( MeshLayout.getDefaultLayout() );
		return cube.create( size );
	}

	/**
	 * Creates a coloured cube.
	 * @param size cube size
	 * @return Coloured cube
	 */
	public static MeshBuilder colouredCube( float size ) {
		final CubeBuilder cube = new CubeBuilder( MeshLayout.create( Primitive.TRIANGLES, "VC", false ) );
		return cube.create( size );

	}

	private final MeshLayout layout;

	/**
	 * Constructor.
	 * @param layout Mesh layout
	 */
	public CubeBuilder( MeshLayout layout ) {
		Check.notNull( layout );
		this.layout = layout;
	}

	/**
	 * Creates a mesh builder for a cube of the given size.
	 * @param size Cube size
	 * @return Cube mesh builder
	 */
	public MeshBuilder create( float size ) {
		final MeshBuilder builder = new MeshBuilder( layout );

		// Build cube with 2 triangles per face (same winding order as we are using triangles)
		for( int n = 0; n < indices.length; n += 4 ) {
			addFace( n, size, builder );
		}

		// Generate normals
		if( builder.hasNormals() ) {
			builder.computeNormals();
		}

		builder.build();

		return builder;
	}

	/**
	 * Adds a cube face.
	 */
	private void addFace( int start, float size, MeshBuilder builder ) {
		// Create face
		final Point[] corners = new Point[ 4 ];
		for( int n = 0; n < 4; ++n ) {
			final int idx = indices[ start + n ];
			final Point pt = new Point( vertices[ idx ] );
			corners[ n ] = pt.multiply( size );
		}
		final Quad quad = new Quad( corners );

		// Set optional colour
		final Colour col = colours[ start / 4 ];
		quad.setColour( col );

		// Set texture coords
		quad.setDefaultTextureCoords();

		// Add face
		builder.addQuad( quad );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
