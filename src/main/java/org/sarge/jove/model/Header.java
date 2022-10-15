package org.sarge.jove.model;

import org.sarge.jove.common.Layout;

/**
 * A <i>model header</i> specifies the structure of a model.
 * @author Sarge
 */
public interface Header {
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
