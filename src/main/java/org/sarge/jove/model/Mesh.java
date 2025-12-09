package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.List;

import org.sarge.jove.common.Layout;

/**
 * A <i>mesh</i> is a model comprising vertices and an optional index buffer.
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

	/**
	 * Vertex or index data.
	 */
	interface MeshData {
		/**
		 * @return Data length (bytes)
		 */
		int length();

		/**
		 * Writes this data to the given buffer.
		 * @param buffer Buffer to write
		 */
		void buffer(ByteBuffer buffer);
	}

	/**
	 * @return Vertex data
	 */
	MeshData vertices();
}
