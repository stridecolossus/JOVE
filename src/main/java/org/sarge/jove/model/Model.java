package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Vertex.Layout;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is comprised of a vertex buffer and an optional index buffer structured according to the given header.
 * @author Sarge
 */
public interface Model {
	/**
	 * A <i>model header</i> is a descriptor for the rendering properties of the model.
	 */
	record Header(Primitive primitive, Layout layout, boolean clockwise) {
		/**
		 * Constructor.
		 * @param primitive			Drawing primitive
		 * @param layout			Vertex layout
		 * @param clockwise			Triangle winding order
		 * @throws IllegalArgumentException if the vertex layout contains normals which are not supported by the drawing primitive
		 */
		public Header {
			Check.notNull(primitive);
			Check.notNull(layout);

			if(!primitive.hasNormals() && layout.components().contains(Vertex.Component.NORMAL)) {
				throw new IllegalArgumentException("Drawing primitive does not support normals: " + primitive);
			}
		}
	}

	/**
	 * @return Descriptor for this model
	 */
	Header header();

	/**
	 * @return Number of vertices
	 */
	int count();

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

	/**
	 * Partial implementation that also applies validation.
	 */
	abstract class AbstractModel implements Model {
		private final Header header;
		private final int count;

		/**
		 * Constructor.
		 * @param header		Model header
		 * @param count			Number of vertices
		 * @throws IllegalArgumentException if the number of vertices is invalid for the drawing primitive
		 */
		protected AbstractModel(Header header, int count) {
			this.header = notNull(header);
			this.count = zeroOrMore(count);
			validate();
		}

		private void validate() {
			if(!header.primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, header.primitive));
			}
		}

		@Override
		public final Header header() {
			return header;
		}

		@Override
		public final int count() {
			return count;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(header)
					.append("count", count)
					.append("indexed", isIndexed())
					.build();
		}
	}
}
