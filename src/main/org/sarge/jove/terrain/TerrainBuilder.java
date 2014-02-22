package org.sarge.jove.terrain;

import java.util.Iterator;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultBufferDataType;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Builder for terrain meshes.
 * @author Sarge
 */
public class TerrainBuilder {
	// Config
	private final MeshBuilder builder;
	private final Weighting[] weightings;

	// Scaling
	private float terrainScale = 1;
	private float heightScale = 1;

	/**
	 * Constructor.
	 * @param layout		Mesh layout used to generate terrain
	 * @param weightings	Multi-texture weightings
	 */
	public TerrainBuilder( MeshLayout layout, Weighting[] weightings ) {
		Check.notNull( layout );
		Check.notNull( weightings );

		this.builder = new MeshBuilder( layout );
		this.weightings = weightings.clone();
	}

	/**
	 * Sets the scaling factor for vertices in the X-Z plane.
	 * @param terrainScale Terrain size scale (default is one)
	 */
	public void setTerrainScale( float terrainScale ) {
		this.terrainScale = terrainScale;
	}

	/**
	 * Sets the scaling factor for terrain height.
	 * @param heightScale Height scalar (default is one)
	 */
	public void setHeightScale( float heightScale ) {
		this.heightScale = heightScale;
	}

	/**
	 * Builds a terrain mesh using the given height-map data.
	 * <p>
	 * The generated terrain has the following properties:
	 * <ul>
	 * <li>orientated in the X-Z plane with heights in the Y direction</li>
	 * <li>centred on the supplied coordinates</li>
	 * <li>the top-left of the height-map is maximum -X / -Z coordinate.
	 * </ul>
	 * @param map Height-map
	 * @return Terrain mesh
	 * @throws IllegalArgumentException if the layout has no normals or weightings are required but the layout has no colours
	 */
	public MeshBuilder build( HeightMap map ) {
		// Calc centre offset
		final int w = 1500; //map.getWidth();
		final int h = 1500; //map.getHeight();
		final float cx = ( w - 1 ) / 2f;
		final float cy = ( h - 1 ) / 2f;

		// Calc texture coord increments (ensure top/right-most coords are 0..1)
		final float dx = 1f / ( w - 1 );
		final float dy = 1f / ( h - 1 );

		// Create terrain vertices
		float tx = 0;
		for( int y = 0; y < h; ++y ) {
			float ty = 0;
			for( int x = 0; x < w; ++x ) {
				// Lookup height at this vertex
				final float height = map.getHeight( x, y );

				// Build vertex position
				final float vx = ( x - cx ) * terrainScale;
				final float vy = height * heightScale;
				final float vz = ( y - cy ) * terrainScale;
				final Point pt = new Point( vx, vy, vz );

				// Build vertex texture coordinate
				final TextureCoord coords = new TextureCoord( tx, ty );
				ty += dy;

				// Add vertex
				final Vertex v = new Vertex( pt );
				v.setNormal( new Vector() );
				v.setTextureCoords( coords );
				builder.add( v );
			}

			tx += dx;
		}

		// Generate triangle-strip for each row
		final int rows = h - 1;
		for( int y = 0; y < rows; ++y ) {
			// Calc start index of next row
			final int start = y * w;

			// Repeat last/next indices to create four degenerate triangles maintaining the winding order before the next row
			if( y > 0 ) {
				builder.addIndex( start + w - 1 );
				builder.addIndex( start );
			}

			// Add next row
			builder.addStrip( start, w );

			// Generate terrain normals (ignoring degenerates)
			final int idx = y * ( ( w * 2 ) + 2 );
			builder.computeNormals( idx, idx + w * 2 - 2 );
		}

		// Normalize all terrain normals
		builder.normalize();

		// Generate multi-texturing weights
		if( !builder.getLayout().contains( DefaultBufferDataType.COLOURS ) ) throw new IllegalArgumentException( "No colour component for multi-texture weightings" );
		final float[] weights = new float[ weightings.length ];
		final Iterator<Vertex> itr = builder.getVertices().iterator();
		for( int y = 0; y < h; ++y ) {
			for( int x = 0; x < w; ++x ) {
				// Calculate texture blending weightings for this vertex
				final Vertex vertex = itr.next();
				final float ht = map.getHeight( x, y ) / 255f; // TODO - proper scaling
				generateWeights( ht, vertex, weights );

				// Normalise weights
				float total = 0;
				for( float f : weights ) {
					total += f;
				}
				if( total > 0 ) {
					for( int n = 0; n < weights.length; ++n ) {
						weights[ n ] = weights[ n ] / total;
					}
				}

				// Clamp weights
				for( int n = 0; n < weights.length; ++n ) {
					weights[ n ] = MathsUtil.clamp( weights[ n ] );
				}

				// Set weightings as colour component of the VBO
				final Colour col = new Colour( weights[0], weights[1], weights[2], 1 ); // TODO - assumes 3 textures
				vertex.setColour( col );
			}
		}

		return builder;
	}

	/**
	 * Generates slope-based multi-texture weightings for the given vertex.
	 * @param h			Height (scaled to 0..1)
	 * @param v			Vertex
	 * @param weights	Output weights
	 */
	protected void generateWeights( float h, Vertex v, float[] weights ) {
		for( int n = 0; n < weights.length; ++n ) {
			weights[ n ] = weightings[ n ].getWeight( h );
		}

		// TODO
//		final float slope = v.getNormal().getY();
//		final float slope = Vector.Y_AXIS.dot( v.getNormal() );
	//	weights[ 0 ] += slope;
	//	weights[ 1 ] += 1 - slope;
//		weights[ 2 ] = weightings[ 2 ].getWeight( h );

//		weights[ 1 ] += v.getNormal().getY();
//		//float fTexture0Contribution = glm::saturate( glm::dot( m_NormalBuffer[i], UP ) - 0.1f );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
