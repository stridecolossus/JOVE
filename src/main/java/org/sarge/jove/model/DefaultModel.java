package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

/**
 * A <i>default model</i> is used to construct a mesh comprising {@link Vertex} data.
 * TODO
 * - vertex layout
 * - to buffered
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public DefaultModel(Primitive primitive, Layout layout) {
		super(primitive, layout);
	}

	/**
	 * Convenience constructor for a model with the given vertex components.
	 * @param primitive 	Drawing primitive
	 * @param components	Vertex components
	 */
	public DefaultModel(Primitive primitive, Component... components) {
		super(primitive, new Layout(components));
	}

	@Override
	public int count() {
		return vertices.size();
	}

	@Override
	public boolean isIndexed() {
		return false;
	}

	/**
	 * @return Model vertices
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex to this model.
	 * @param vertex Vertex to add
	 * @throws IllegalArgumentException if the layout of the given {@link #vertex} is invalid for this model
	 */
	public DefaultModel add(Vertex vertex) {
		if(!this.layout().equals(vertex.layout())) throw new IllegalArgumentException("Invalid vertex layout: vertex=%s model=%s".formatted(vertex, this));
		vertices.add(vertex);
		return this;
	}

	/**
	 * Mesh vertex buffer.
	 */
	protected final class VertexBuffer implements ByteSizedBufferable {
		@Override
		public int length() {
			final Layout layout = DefaultModel.this.layout();
			return vertices.size() * layout.stride();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			for(Vertex v : vertices) {
				v.buffer(bb);
			}
		}
	}

	/**
	 * Creates a buffered instance of this model.
	 * @return This buffered model
	 */
	public BufferedModel buffer() {
		return new BufferedModel(this, new VertexBuffer(), null);
	}

	/**
	 * Calculates the bounds of this model.
	 * @return Model bounds
	 * @throws IllegalStateException if the model layout does not contain a {@link Point#LAYOUT} component
	 */
	public Bounds bounds() {
		// Determine vertex position from layout
		validate();
		final boolean pos = this.layout().components().stream().anyMatch(e -> e == Point.LAYOUT);
		if(!pos) throw new IllegalStateException("Model layout does not contain a vertex position: " + this);

		// Construct bounds
		final var bounds = new Bounds.Builder();
		for(Vertex v : vertices) {
			final Point p = v.position();
			bounds.add(p);
		}
		return bounds.build();
	}

	public final Stream<Triangle> triangles() {
// TODO - optional operation
//		validate();

		final Primitive primitive = this.primitive();
		if(!primitive.isTriangle()) throw new IllegalStateException("Model does not contain triangular polygons: " + primitive);

		final int faces = primitive.faces(count());
		return IntStream
				.range(0, faces)
				.mapToObj(mapper(primitive))
				.map(this::triangle);
	}

	private static IntFunction<int[]> mapper(Primitive primitive) {
		if(primitive.isStrip()) {
			return face -> new int[]{face, face + 1, face + 2};
		}
		else {
			return face -> {
				final int start = face * 3;
				return new int[]{start, start + 1, start + 2};
			};
		}
	}
	// TODO - move to primitive

	private Triangle triangle(int[] indices) {
		final List<Vertex> vertices = new ArrayList<>(3);
		for(int n = 0; n < 3; ++n) {
			vertices.add(vertex(indices[n]));
		}
		return new Triangle(vertices);
	}

	// TODO - indexed
	protected Vertex vertex(int index) {
		return vertices.get(index);
	}

//
//	/**
//	 * @return Iterator over the polygons of this model
//	 * @throws IllegalStateException if this model does not contain vertex data
//	 * @throws IllegalStateException if the drawing primitive is not {@link Primitive#isPolygon()}
//	 */
//	public Iterator<Polygon> faces() {
//		validate();
//
//		final Primitive primitive = this.primitive();
//		if(!primitive.isPolygon()) throw new IllegalStateException("Model does not contain triangular polygons");
//
//		// TODO
//		final int pos = this.layout().components().indexOf(Point.LAYOUT);
//		if(pos == -1) throw new IllegalStateException("Model layout does not contain a vertex position");
//
//		final int faces = primitive.faces(count());
//
//		return new Iterator<>() {
//			private final int[] indices = new int[3];
//			private int next;
//
//			@Override
//			public boolean hasNext() {
//				return next < faces;
//			}
//
//			@Override
//			public Polygon next() {
//				if(!hasNext()) throw new NoSuchElementException();
//
//				if(primitive.isStrip()) {
//					final int start = next * 3;
//					for(int n = 0; n < 3; ++n) {
//						indices[n] = start + n;
//					}
//				}
//				else {
//					for(int n = 0; n < 3; ++n) {
//						indices[n] = next + n;
//					}
//				}
//
////				if(header.isIndexed()) {
////    				for(int n = 0; n < 3; ++n) {
////    					indices[n] = index.get(indices[n]);
////    				}
////				}
//
//				++next;
//
//				// TODO
//				final List<Point> triangle = Arrays
//						.stream(indices)
//						.mapToObj(vertices::get)
//						.map(v -> (Point) v.component(pos))
//						.toList();
//
//				return new Polygon(triangle);
//			}
//		};
//	}
//
//	/**
//	 * Computes the vertex normals for this model.
//	 * @throws IllegalStateException if the layout of this model does not contain vertex data or already contains normals
//	 */
//	public DefaultModel compute() {
////		if(!header.components.contains(Point.LAYOUT)) throw new IllegalStateException("Model does not contain vertices");
////		if(isNormalsLayout()) throw new IllegalStateException("Model already contains vertex normals");
////		validate();
////		layout(Normal.LAYOUT);
////
////
////
//		return this;
//	}

	private void validate() {
		final Primitive primitive = this.primitive();
		if(!primitive.isValidVertexCount(this.count())) {
			throw new IllegalStateException("Invalid draw count for primitive: " + this);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("vertices", vertices.size())
				.build();
	}
}
