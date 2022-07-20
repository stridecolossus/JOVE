package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.util.Mask;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is a renderable object comprised vertex data and an optional index.
 * @author Sarge
 */
public class Model {
	/**
	 * Vertex normal layout.
	 */
	public static final Layout NORMALS = Layout.floats(3);

	/**
	 * Descriptor for this model.
	 */
	public record Header(Primitive primitive, int count, List<Layout> layout) {
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
			layout = List.copyOf(layout);

			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
			}

			final boolean normals = layout.stream().anyMatch(e -> e == NORMALS);
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

	private final Header header;
	private final Bufferable vertices;
	private final Bufferable index;

	/**
	 * Constructor.
	 * @param header		Model header
	 * @param vertices		Vertex data
	 * @param index			Optional index
	 */
	public Model(Header header, Bufferable vertices, Bufferable index) {
		this.header = notNull(header);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	/**
	 * @return Model header
	 */
	public Header header() {
		return header;
	}

	/**
	 * @return Vertex data
	 */
	public Bufferable vertices() {
		return vertices;
	}

	/**
	 * @return Index
	 */
	public Optional<Bufferable> index() {
		return Optional.ofNullable(index);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Model that) &&
				this.header.equals(that.header()) &&
				this.vertices.equals(that.vertices()) &&
				this.index().equals(that.index());
	}

	@Override
	public String toString() {
		return header.toString();
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

			// Create model
			final Header header = new Header(primitive, count, layout);
			final Bufferable data = vertices();
			return new Model(header, data, indices);
		}

		/**
		 * Constructs the vertex buffer.
		 */
		private Bufferable vertices() {
			return new Bufferable() {
				@Override
				public int length() {
					return Layout.stride(layout) * vertices.size();
				}

				@Override
				public void buffer(ByteBuffer bb) {
					for(Bufferable b : vertices) {
						b.buffer(bb);
					}
				}
			};
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
