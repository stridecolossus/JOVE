package org.sarge.jove.model;

import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>model</i> is comprised of a vertex buffer with a specified layout and an optional index buffer.
 * @author Sarge
 */
public interface Model {
	/**
	 * @return Vertex layout
	 */
	List<Layout> layout();

	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Draw count
	 */
	int count();

	/**
	 * @return Vertex buffer
	 */
	Bufferable vertexBuffer();

	/**
	 * @return Whether this is an indexed model
	 * @see #index()
	 */
	boolean isIndexed();

	/**
	 * @return Index buffer
	 * @see #isIndexed()
	 */
	Bufferable indexBuffer();
}
