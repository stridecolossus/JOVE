package org.sarge.jove.model;

import org.sarge.jove.common.CompoundLayout;

/**
 * A <i>mesh</i> specifies the structure of a renderable object.
 * @author Sarge
 */
public interface Mesh {
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
	CompoundLayout layout();

	/**
	 * @return Whether this mesh has an index
	 */
	boolean isIndexed();
}
