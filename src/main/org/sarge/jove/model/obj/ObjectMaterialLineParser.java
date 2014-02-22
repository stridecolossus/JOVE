package org.sarge.jove.model.obj;

import org.sarge.jove.material.MutableMaterial;

/**
 * Parser for an <tt>OBJ</tt> material file.
 * @author Sarge
 */
public interface ObjectMaterialLineParser {
	/**
	 * Parses a line of a material file.
	 * @param args		Arguments
	 * @param mat		Material
	 */
	void parse( String[] args, MutableMaterial mat );
}
