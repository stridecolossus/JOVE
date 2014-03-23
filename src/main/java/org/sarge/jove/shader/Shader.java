package org.sarge.jove.shader;

import org.sarge.jove.common.GraphicResource;

/**
 * Shader.
 * @author Sarge
 */
public interface Shader extends GraphicResource {
	/**
	 * Shader type.
	 */
	enum Type {
		VERTEX( "vert" ),
		FRAGMENT( "frag" ),
		GEOMETRY( "geom" ),
		TESSELATION_EVALUATION( "eval" ),
		TESSELATION( "tess" );

		private final String ext;

		private Type( String ext ) {
			this.ext = ext;
		}

		/**
		 * Looks up the shader type for the given filename extension.
		 * @param ext Filename extension
		 * @return Shader type or <tt>null</tt> if not known
		 */
		public static Type getType( String ext ) {
			for( Type t : values() ) {
				if( t.ext.equals( ext ) ) return t;
			}

			return null;
		}
	}
}
