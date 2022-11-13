package org.sarge.jove.model;

import org.sarge.jove.common.Layout;

/**
 * A <i>model</i> specifies the structure of a renderable mesh.
 * @author Sarge
 */
public interface Model {
	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Draw count
	 */
	int count();

	/**
	 * @return Vertex layout
	 */
	Layout layout();

	/**
	 * @return Whether this model has an index
	 */
	boolean isIndexed();
}
