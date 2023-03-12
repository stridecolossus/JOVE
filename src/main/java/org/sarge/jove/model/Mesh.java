package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.*;

/**
 * A <i>mesh</i> is a renderable model comprised of {@link Vertex} data and an optional index.
 * @author Sarge
 */
public interface Mesh {
	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Vertex layout
	 */
	CompoundLayout layout();

	/**
	 * @return Draw count
	 */
	int count();

	/**
	 * @return Vertex buffer
	 */
	ByteSizedBufferable vertices();

	/**
	 * @return Whether this mesh has an index
	 */
	boolean isIndexed();

	/**
	 * @return Index buffer
	 */
	Optional<ByteSizedBufferable> index();
}
