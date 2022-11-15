package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>default model</i> is used to construct a mesh comprising {@link Vertex} data.
 * TODO
 * - vertex layout
 * - to buffered
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices = new ArrayList<>();
	private boolean validate = true;

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
	 * Sets whether to validate the layout of vertices against this model (default is {@code true}).
	 * @param validate Whether to validate
	 * @see #add(Vertex)
	 */
	public DefaultModel validate(boolean validate) {
		this.validate = validate;
		return this;
	}

	/**
	 * Adds a vertex to this model.
	 * @param vertex Vertex to add
	 * @throws IllegalArgumentException if the layout of the given {@link #vertex} is invalid for this model
	 * @see #validate(boolean)
	 */
	public DefaultModel add(Vertex vertex) {
		if(validate && !this.layout().equals(vertex.layout())) {
			throw new IllegalArgumentException("Invalid vertex layout: vertex=%s model=%s".formatted(vertex, this));
		}
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
		// TODO - parallel? requires spliterator to join bounds?
	}

	/**
	 * @return Triangles indices for this model
	 * @throws IllegalStateException if the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	private Stream<int[]> indices() {
		final Primitive primitive = this.primitive();
		if(!primitive.isTriangle()) throw new IllegalStateException("Model does not contain triangular polygons: " + primitive);
		final int faces = primitive.faces(count());
		return IntStream.range(0, faces).mapToObj(primitive::indices);
	}
	// TODO - could be parallel stream operation?

	/**
	 * @return Triangles for this model
	 * @throws IllegalStateException if the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	public Stream<Triangle> triangles() {
		return indices().map(this::triangle).map(Triangle::new);
	}

	/**
	 * Maps the given indices to vertex positions.
	 * @param indices Triangle indices
	 * @return Triangle points
	 */
	private List<Point> triangle(int[] indices) {
		return Arrays
				.stream(indices)
				.map(this::index)
				.mapToObj(vertices::get)
				.map(Vertex::position)
				.toList();
	}

	/**
	 * Maps the given triangle index to a vertex index.
	 * @param index Triangle index
	 * @return Vertex index
	 */
	protected int index(int index) {
		return index;
	}

	/**
	 * Computes vertex normals for this model.
	 * @throws IllegalStateException if normals cannot be generated for this moel
	 */
	public void compute() {
		// Validate normals can be computed
		final Layout layout = this.layout();
		if(!layout.contains(Point.LAYOUT)) throw new IllegalStateException("Model does not contain vertices");
		if(!layout.contains(Normal.LAYOUT)) throw new IllegalStateException("Model does not contain vertex normals");
		validate();

		/**
		 * Helper.
		 */
		class Compute {
			private final float[][] normals = new float[vertices.size()][3];

			/**
			 * Accumulates triangle normals.
			 */
			void add(int[] indices) {
				// Calculate triangle normal
				final Triangle triangle = new Triangle(triangle(indices));
				final var normal = triangle.normal();

				// Accumulate vertex normals
				for(int n = 0; n < indices.length; ++n) {
					add(indices[n], normal);
				}
			}
			// TODO - cannot be parallel

			private void add(int index, Vector n) {
				final float[] normal = normals[index];
				normal[0] += n.x;
				normal[1] += n.y;
				normal[2] += n.z;
			}

			/**
			 * Populates the accumulated vertex normals.
			 */
			void compute() {
				for(int n = 0; n < normals.length; ++n) {
					final Vertex v = vertices.get(n);
					final Normal normal = new Normal(new Vector(normals[n]));
					v.normal(normal);
				}
			}
			// TODO - could be parallel stream operation
		}

		// Accumulate vertex normals
		final Compute compute = new Compute();
		this.indices().forEach(compute::add);
		compute.compute();
	}

	// TODO - segments
	// - add segment to VBO/index
	// - calc normals for segment
	// - delete? insert? replace?
	// - factor out to separate class?

	/**
	 * @throws IllegalStateException if this model is not valid for rendering
	 */
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
