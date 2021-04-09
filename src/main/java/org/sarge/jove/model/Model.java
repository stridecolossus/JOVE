package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is comprised of a vertex buffer and an optional index buffer that are structured according to the given header.
 * @author Sarge
 */
public interface Model {
	/**
	 * A <i>model header</i> is a descriptor for the rendering properties of the model.
	 */
	record Header(Primitive primitive, boolean clockwise, int count) {
		/**
		 * Constructor.
		 * @param primitive			Drawing primitive
		 * @param clockwise			Triangle winding order
		 * @param count				Number of vertices
		 * @throws IllegalArgumentException if the number of vertices is invalid for the given primitive
		 */
		public Header {
			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
			}
			Check.notNull(primitive);
			Check.zeroOrMore(count);
		}
	}

	/**
	 * @return Descriptor for this model
	 */
	Header header();

	/**
	 * @return Whether this is an indexed model
	 */
	boolean isIndexed();

	/**
	 * @return Vertex buffer
	 */
	Bufferable vertexBuffer();

	/**
	 * @return Index buffer
	 */
	Optional<Bufferable> indexBuffer();
}
