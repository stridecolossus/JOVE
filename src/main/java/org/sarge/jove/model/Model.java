package org.sarge.jove.model;

import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtil;

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
	 * Maximum length of a {@code short} index buffer.
	 */
	long SHORT = MathsUtil.unsignedMaximum(Short.SIZE);

	/**
	 * Determines whether the appropriate data type for the given index buffer length.
	 * @param len Index length
	 * @return Whether the given index length is represented as {@code int} or {@code short} indices
	 */
	static boolean isIntegerIndex(long len) {
		return len >= SHORT;
	}
}
