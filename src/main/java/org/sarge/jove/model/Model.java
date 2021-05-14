package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.ByteSource;
import org.sarge.jove.common.Component.Layout;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is comprised of a vertex buffer and an optional index buffer structured according to the given header.
 * @author Sarge
 */
public interface Model {
	/**
	 * Descriptor for this model.
	 */
	record Header(List<Layout> layout, Primitive primitive, int count, boolean clockwise) {
		/**
		 * Constructor.
		 * @param layout			Vertex layout
		 * @param primitive			Drawing primitive
		 * @param count				Number of vertices
		 * @param clockwise			Triangle winding order
		 * @throws IllegalArgumentException if the {@link #count} is invalid for the given {@link #primitive}
		 * @see Primitive#isValidVertexCount(int)
		 * @see Primitive#isNormalSupported()
		 */
		public Header {
			Check.notNull(layout);
			Check.notNull(primitive);
			Check.zeroOrMore(count);

			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
			}

			// TODO - check primitive supports normals
			// primitive.hasNormals()
		}

		/**
		 * @return Vertex stride (bytes)
		 */
		public int stride() {
			return layout.stream().mapToInt(Layout::length).sum();
		}

		/**
		 * @return Vertex buffer length (bytes)
		 */
		public int length() {
			return count * stride();
		}
	}

	/**
	 * @return Model header
	 */
	Header header();

	/**
	 * @return Vertex buffer
	 */
	ByteSource vertexBuffer();

	/**
	 * @return Whether this is an indexed model
	 */
	boolean isIndexed();

	/**
	 * @return Index buffer
	 */
	Optional<ByteSource> indexBuffer();

	// TODO - VBO and index should be byte stream wrapper of some sort

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractModel implements Model {
		protected final Header header;

		/**
		 * Constructor.
		 * @param header Model header
		 */
		protected AbstractModel(Header header) {
			this.header = notNull(header);
		}

		@Override
		public final Header header() {
			return header;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(header)
					.append("indexed", isIndexed())
					.build();
		}
	}
}
