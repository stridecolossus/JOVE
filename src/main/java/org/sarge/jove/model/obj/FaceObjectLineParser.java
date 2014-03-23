package org.sarge.jove.model.obj;

import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;

/**
 * Parser for a face polygon.
 * @author Sarge
 */
public class FaceObjectLineParser implements ObjectLineParser {
	@Override
	public void parse( String[] args, ObjectModelData data ) {
		for( String str : args ) {
			// Tokenize vertex components
			final String[] parts = str.split( "/" );

			// Lookup vertex position
			final int idx = Integer.parseInt( parts[ 0 ] );
			final Point pos = data.getVertex( idx );

			// Add vertex to model
			final Vertex v = new Vertex( pos );
			data.add( v );

			// Add optional texture coordinates
			if( parts.length > 1 ) {
				if( parts[ 1 ].length() > 0 ) {
					final int coordsIndex = Integer.parseInt( parts[ 1 ] );
					final TextureCoord coords = data.getTextureCoord( coordsIndex );
					v.setTextureCoords( coords );
				}
			}

			// Add normal
			if( parts.length == 3 ) {
				final int normalIndex = Integer.parseInt( parts[ 2 ] );
				final Vector normal = data.getNormal( normalIndex );
				v.setNormal( normal );
			}
		}
	}
}
