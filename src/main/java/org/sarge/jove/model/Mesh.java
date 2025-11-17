package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;

/**
 * A <i>mesh</i> is a renderable model comprising vertices and an optional index buffer.
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
	List<Layout> layout();

	/**
	 * @return Draw count
	 */
	int count();

	// TODO - vertices() and index() do not RETURN a buffer but ARE bufferable
	// i.e. Mesh implements Bufferable
	// NB also works for a persisted mesh

	/**
	 * @return Vertex buffer
	 */
	ByteBuffer vertices();

	/**
	 * @return Index buffer
	 */
	Optional<ByteBuffer> index();
}
