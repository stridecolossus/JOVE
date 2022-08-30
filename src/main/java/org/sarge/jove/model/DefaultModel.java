package org.sarge.jove.model;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;

/**
 * Default implementation for a simple model.
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final int count;
	private final Bufferable vertices;
	private final Bufferable index;

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param count			Draw count
	 * @param layout		Vertex layout
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 * @throws IllegalArgumentException if the layout contains {@link Model#NORMALS} but normals are not supported by the given {@link #primitive}
	 * @throws IllegalArgumentException if {@link #count} is invalid for the drawing primitive of this model
	 * @see Primitive#isValidVertexCount(int)
	 */
	public DefaultModel(Primitive primitive, int count, CompoundLayout layout, Bufferable vertices, Bufferable index) {
		super(primitive, layout);
		validate(count);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public Bufferable vertices() {
		return vertices;
	}

	@Override
	public Optional<Bufferable> index() {
		return Optional.ofNullable(index);
	}

	// TODO - extract bounds from model/builder?

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
		 * @see DefaultModel#Model(Header, Bufferable, Bufferable)
		 */
		public DefaultModel build() {
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

			final CompoundLayout compound = new CompoundLayout(layout);

			// Create vertices
			final var data = new Bufferable() {
				@Override
				public int length() {
					return compound.stride() * vertices.size();
				}

				@Override
				public void buffer(ByteBuffer bb) {
					for(Bufferable b : vertices) {
						b.buffer(bb);
					}
				}
			};

			// Create model
			return new DefaultModel(primitive, count, compound, data, indices);
		}

		/**
		 * Constructs the index buffer.
		 */
		private Bufferable index() {
			return new Bufferable() {
				private final boolean integral = Model.isIntegerIndex(index.size());

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
