package org.sarge.jove.model.obj;

import org.sarge.lib.util.Check;

/**
 * Material library file-path parser.
 * <p>
 * Note that the <tt>mtllib</tt> identifier is usually delimited by a tab character and may contain spaces.
 * Therefore the path has to be re-built from the arguments.
 * @author Sarge
 */
public class MaterialLibraryObjectLineParser implements ObjectLineParser {
	private final ObjectMaterialLoader loader;

	/**
	 * Constructor.
	 * @param loader Material loader
	 */
	public MaterialLibraryObjectLineParser( ObjectMaterialLoader loader ) {
		Check.notNull( loader );
		this.loader = loader;
	}

	@Override
	public void parse( String[] args, ObjectModelData model ) {
		// Rebuild library path
		final StringBuilder path = new StringBuilder();
		for( String str : args ) {
			if( path.length() > 0 ) path.append( ' ' );
			path.append( str );
		}

		// Load material library
		loader.load( path.toString(), model.getMaterialLibrary() );
	}
}
