package org.sarge.jove.model.obj;

/**
 * <tt>OBJ</tt> model parser.
 * @author Sarge
 */
public interface ObjectLineParser {
	/**
	 * Parses the given arguments.
	 * @param args Arguments
	 * @param data Model
	 */
	void parse( String[] args, ObjectModelData data );
}
