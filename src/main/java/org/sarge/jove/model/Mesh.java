package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.List;

import org.sarge.jove.common.*;

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

	/**
	 * A mesh <i>data buffer</i> specifies the properties of the vertices or index of this mesh.
	 */
	interface DataBuffer extends Bufferable {
		/**
		 * @return Data length (bytes)
		 */
		int length();

		@Override
		void buffer(ByteBuffer buffer);
	}

	/**
	 * @return Vertex buffer
	 */
	DataBuffer vertices();
}
