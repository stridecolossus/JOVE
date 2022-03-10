package org.sarge.jove.model;

import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>model</i> is comprised of a vertex buffer with a specified layout and an optional index buffer.
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
