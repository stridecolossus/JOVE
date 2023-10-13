package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

/**
 * A <i>mesh</i> is a renderable model comprising a vertex buffer and optional index.
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
	 * @return Index buffer
	 */
	Optional<ByteSizedBufferable> index();

	/**
	 * Validates that the the drawing primitive and vertex layout are compatible.
	 * @throws IllegalArgumentException if the layout does not contain a vertex position
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	static void validate(Primitive primitive, CompoundLayout layout) {
		if(!layout.contains(Point.LAYOUT)) {
			throw new IllegalArgumentException("Layout does not contain a vertex position: " + layout);
		}

		if(!primitive.isTriangle() && layout.contains(Normal.LAYOUT)) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	/**
	 * Validates that this mesh can be rendered for the given draw count.
	 * @throws IllegalArgumentException if the draw count is not valid for the rendering primitive
	 */
	static int validate(Primitive primitive, int count) {
		if(!primitive.isValidVertexCount(count)) {
			throw new IllegalArgumentException(String.format("Invalid draw count %d for primitive %s", count, primitive));
		}
		return count;
	}
}
