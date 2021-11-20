package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.io.Bufferable;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is comprised of a vertex buffer and an optional index buffer structured according to the given header.
 * @author Sarge
 */
public interface Model {
	/**
	 * Special case layout for vertex normals.
	 */
	Layout NORMALS = Layout.of(3);

	/**
	 * Descriptor for this model.
	 */
	record Header(List<Layout> layout, Primitive primitive, int count) {
		/**
		 * Constructor.
		 * @param layout			Vertex layout
		 * @param primitive			Drawing primitive
		 * @param count				Number of vertices
		 * @throws IllegalArgumentException if the {@link #count} is invalid for the given {@link #primitive}
		 * @throws IllegalArgumentException if the layout contains {@link Vector#NORMALS} and the primitive does not support normals
		 * @see Primitive#isValidVertexCount(int)
		 * @see Primitive#isNormalSupported()
		 */
		public Header {
			layout = List.copyOf(layout);
			Check.notNull(primitive);
			Check.zeroOrMore(count);

			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
			}

			if(!primitive.isNormalSupported() && layout.stream().anyMatch(e -> e == NORMALS)) {
				throw new IllegalArgumentException("Normals not supported for primitive: " + primitive);
			}
		}
	}

	/**
	 * @return Model header
	 */
	Header header();

	/**
	 * @return Vertex buffer
	 */
	Bufferable vertices();

	/**
	 * @return Whether this is an indexed model
	 */
	boolean isIndexed();

	/**
	 * @return Index buffer
	 */
	Optional<Bufferable> index();

	/**
	 * Transforms this model to the given component layout.
	 * @param layouts Component layout
	 * @return Transformed model
	 * @throws IllegalArgumentException if this model is not comprised of the given layouts
	 */
	Model transform(List<Layout> layout);

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
		public Model transform(List<Layout> layouts) {
			throw new UnsupportedOperationException();
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
