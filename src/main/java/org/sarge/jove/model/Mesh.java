package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

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
	 * Vertex data.
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

	/**
	 * An <i>index</i> outputs the vertex indices for this mesh.
	 * <p>
	 * Note that by default an index is comprised of 32-bit integers.
	 * The {@link #index(int)} method can be used to select a smaller element size as required.
	 * The minimum number of bytes per element for a given index is specified by {@link #minimumElementBytes()}.
	 */
	interface Index extends MeshData {
		/**
		 * @return Minimum number of bytes required for the values of this index
		 */
		int minimumElementBytes();

		/**
		 * Selects a smaller element byte size for this index.
		 * @param bytes Required byte size
		 * @return Index with the given size
		 * @throws IllegalArgumentException if {@link #bytes} is smaller than {@link #minimumElementBytes()}
		 */
		Index index(int bytes);
	}

	/**
	 * @return Drawing index
	 */
	Optional<Index> index();
}
