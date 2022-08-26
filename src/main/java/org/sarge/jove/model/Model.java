package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.util.Mask;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is a renderable object comprised vertex data and an optional index.
 * @author Sarge
 */
@SuppressWarnings("unused")
public interface Model {
	/**
	 * @return Model header
	 */
	Header header();

	/**
	 * @return Vertex data
	 */
	Bufferable vertices();

	/**
	 * @return Index
	 */
	Optional<Bufferable> index();

	/**
	 * Vertex normal layout.
	 */
	Layout NORMALS = Layout.floats(3);

	/**
	 * Descriptor for this model.
	 */
	record Header(Primitive primitive, int count, CompoundLayout layout) {
		/**
		 * Maximum length of a {@code short} index buffer.
		 */
		private static final long SHORT = Mask.unsignedMaximum(Short.SIZE);

		/**
		 * Constructor.
		 * @param primitive		Drawing primitive
		 * @param count			Draw count
		 * @param layout		Vertex layout
		 * @throws IllegalArgumentException if {@link #count} is invalid for the given drawing primitive
		 * @throws IllegalArgumentException if the layout contains {@link Vertex#NORMALS} but normals are not supported by the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 * @see Primitive#isNormalSupported()
		 */
		public Header {
			Check.notNull(primitive);
			Check.zeroOrMore(count);
			Check.notNull(layout);

			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
			}

			final boolean normals = layout.layouts().stream().anyMatch(e -> e == NORMALS);
			if(normals && !primitive.isNormalSupported()) {
				throw new IllegalArgumentException("Vertex normals are not supported by primitive: " + primitive);
			}
		}

		/**
		 * Determines where the given draw count requires an {@code int} or {@code short} index.
		 * @param count Draw count
		 * @return Index type
		 */
		public static boolean isIntegerIndex(int count) {
			return count >= SHORT;
		}
	}

	/**
	 * Default implementation for a static model.
	 */
	class DefaultModel implements Model {
		private final Header header;
		private final Bufferable vertices;
		private final Bufferable index;

		/**
		 * Constructor.
		 * @param header		Model header
		 * @param vertices		Vertex data
		 * @param index			Optional index
		 */
		public DefaultModel(Header header, Bufferable vertices, Bufferable index) {
			this.header = notNull(header);
			this.vertices = notNull(vertices);
			this.index = index;
		}

		@Override
		public Header header() {
			return header;
		}

		@Override
		public Bufferable vertices() {
			return vertices;
		}

		@Override
		public Optional<Bufferable> index() {
			return Optional.ofNullable(index);
		}

		@Override
		public String toString() {
			return header.toString();
		}
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private final List<Layout> layout = new ArrayList<>();
		private final List<Vertex> vertices = new ArrayList<>();
		private final List<Integer> index = new ArrayList<>();

		/**
		 * Sets the drawing primitive for this model.
		 * @param primitive Drawing primitive
		 */
		public Builder primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			return this;
		}

		/**
		 * Adds a vertex component layout.
		 * @param layout Vertex component layout
		 */
		public Builder layout(Layout layout) {
			this.layout.add(notNull(layout));
			return this;
		}

		/**
		 *
		 * @return
		 */
		public List<?> filter() {
			final Map<Layout, Class<?>> filter = Map.of(
					Point.LAYOUT,			Point.class,
//					NORMALS,				Vector.class,
					Coordinate2D.LAYOUT,	Coordinate2D.class,
					Colour.LAYOUT,			Colour.class
			);

			return layout
					.stream()
					.map(filter::get)
					.filter(Objects::nonNull)
					.toList();
		}

		/**
		 * Adds a vertex to this model.
		 * @param vertex Vertex
		 */
		public Builder add(Vertex vertex) {
			vertices.add(notNull(vertex));
			return this;
		}

		/**
		 * Adds an index.
		 * @param index Index
		 * @throws IllegalArgumentException if the index is invalid for this model
		 */
		public Builder add(int index) {
			if((index < 0) || (index >= vertices.size())) throw new IllegalArgumentException("Invalid index for this model");
			this.index.add(index);
			return this;
		}

		/**
		 * Adds a primitive restart index.
		 */
		public Builder restart() {
			index.add(-1);
			return this;
		}
		// TODO - this screws the count test in the model header

		/**
		 * Constructs this model.
		 * @return New model
		 * @see Model#Model(Header, Bufferable, Bufferable)
		 */
		public Model build() {
			// Determine whether indexed
			final int count;
			final Bufferable indices;
			if(index.isEmpty()) {
				count = vertices.size();
				indices = null;
			}
			else {
				count = index.size();
				indices = index();
			}

			// Init model
			final Header header = new Header(primitive, count, new CompoundLayout(layout));

			// Create vertices
			final var data = new Bufferable() {
				@Override
				public int length() {
					return header.layout().stride() * vertices.size();
				}

				@Override
				public void buffer(ByteBuffer bb) {
					for(Bufferable b : vertices) {
						b.buffer(bb);
					}
				}
			};

			// Create model
			return new DefaultModel(header, data, indices);
		}

		/**
		 * Constructs the index buffer.
		 */
		private Bufferable index() {
			return new Bufferable() {
				private final boolean integral = Header.isIntegerIndex(index.size());

				@Override
				public int length() {
					return index.size() * (integral ? Integer.BYTES: Short.BYTES);
				}

				@Override
				public void buffer(ByteBuffer bb) {
					if(integral) {
						if(bb.isDirect()) {
							// Write index to direct integer buffer
							for(int n : index) {
								bb.putInt(n);
							}
						}
						else {
							// Write index to non-direct integer buffer
							final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
							bb.asIntBuffer().put(array);
						}
					}
					else {
						// Otherwise write index as a short buffer
						for(int n : index) {
							bb.putShort((short) n);
						}
					}
				}
			};
		}
	}
}
