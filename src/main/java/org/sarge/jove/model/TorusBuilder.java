package org.sarge.jove.model;

import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Builder for a torus.
 * @author Sarge
 */
public class TorusBuilder {
	/**
	 * Creates a torus.
	 * @param ring		Number of rings
	 * @param num		Number of points in each ring
	 * @param inner		Inner radius
	 * @param outer		Outer radius
	 * @return Torus mesh
	 */
	public static MeshBuilder create( int rings, int num, float inner, float outer ) {
		Check.oneOrMore( rings );
		Check.oneOrMore( num );

		// Create model
		final IndexedMeshBuilder builder = new IndexedMeshBuilder( MeshLayout.create( Primitive.TRIANGLE_STRIP, "VN0", false ) );

		// Build torus vertices
		final float r = ( outer - inner ) / 2f;
		final float thetaStep = MathsUtil.TWO_PI / rings;
		final float phiStep = MathsUtil.TWO_PI / num;
		final float ringStep = 1f / rings;
		final float radialStep = 1f / num;
		float t = 0;
		for( float theta = 0; theta < MathsUtil.TWO_PI; theta += thetaStep ) {
			// Calc centre point of ring
			final float sinTheta = MathsUtil.sin( theta );
			final float cosTheta = MathsUtil.cos( theta );

			// Build ring vertices
			float s = 0;
			for( float phi = 0; phi < MathsUtil.TWO_PI; phi += phiStep ) {
				// Calc distance from torus centre
				final float sinPhi = MathsUtil.sin( phi );
				final float cosPhi = MathsUtil.cos( phi );
				final float d = inner + r + ( r * cosPhi );

				// Create vertex
				final Point pos = new Point( cosTheta * d, sinTheta * d, r * sinPhi );
				final Vector normal = new Vector( cosTheta * cosPhi, sinTheta * cosPhi, sinPhi );
				final TextureCoord coords = new TextureCoord( s, t );

				// Add vertex to model
				final Vertex v = new Vertex( pos );
				v.setNormal( normal );
				v.setTextureCoords( coords );
				builder.add( v );

				// Inc cylinder coord
				s += radialStep;
			}

			// Inc ring coord
			t += ringStep;
		}

		// Build cylinder for each ring
		for( int n = 0; n < rings - 1; ++n ) {
			builder.addStrip( n * num, num );
		}

		// Build final cylinder linking end ring to start
		final int end = ( rings - 1 ) * num;
		for( int n = 0; n < num; ++n ) {
			builder.addIndex( end + n );
			builder.addIndex( n );
		}

		return builder;
	}
}
