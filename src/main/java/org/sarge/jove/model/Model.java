package org.sarge.jove.model;

import java.util.*;

import org.sarge.jove.common.*;

/**
 * A <i>model</i> is comprised of a vertex buffer with a specified layout and an optional index buffer.
 *
 * TODO
 * - no point in keeping vertices/index in memory once its loaded to the GPU
 * - factor out header again?
 *
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
	List<Layout> layout();

	/**
	 * @return Vertex buffer
	 */
	Bufferable vertexBuffer();

	/**
	 * @return Index buffer
	 */
	Optional<Bufferable> indexBuffer();

	/**
	 * @return Whether the index is comprised or {@code int} or {@code short} indices
	 */
	boolean isIntegerIndex();
}
