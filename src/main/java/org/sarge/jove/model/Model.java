package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.*;
import org.sarge.jove.util.Mask;

/**
 * A <i>model</i> is a renderable object comprised vertex data and an optional index.
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
	Layout layout();

	/**
	 * @return Vertex data
	 */
	Bufferable vertices();

	/**
	 * @return Whether this model has an index
	 */
	default boolean isIndexed() {
		return index().isPresent();
	}

	/**
	 * @return Index
	 */
	Optional<Bufferable> index();

	/**
	 * Vertex normal layout.
	 */
	Component NORMALS = Component.floats(3);

	/**
	 * Determines whether the given draw count requires an {@code int} or {@code short} index.
	 * @param count Draw count
	 * @return Index type
	 */
	static boolean isIntegerIndex(int count) {
		return count >= Mask.unsignedMaximum(Short.SIZE);
	}
}
