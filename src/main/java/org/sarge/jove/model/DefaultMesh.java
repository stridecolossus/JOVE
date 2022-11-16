package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.volume.Bounds;

/**
 * A <i>default mesh</i> is used to construct a renderable object comprising {@link Vertex} data.
 * <p>
 * The {@link #buffer()} factory method creates a renderable instance of this mesh with a vertex buffer and optional index buffer.
 * <p>
 * Vertex normals can be automatically computed using the {@link #compute()} method.
 * <p>
 * @see IndexedMesh
 * @author Sarge
 */
public class DefaultMesh extends AbstractMesh {
	private final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public DefaultMesh(Primitive primitive, Layout layout) {
		super(primitive, layout);
	}

	/**
	 * Convenience constructor for a mesh with the given vertex components.
	 * @param primitive 	Drawing primitive
	 * @param components	Vertex components
	 */
	public DefaultMesh(Primitive primitive, Component... components) {
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
	 * @return Vertices of this mesh
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex to this mesh.
	 * @param vertex Vertex to add
	 * @throws IllegalArgumentException if the layout of the given {@link #vertex} is invalid for this mesh
	 */
	public DefaultMesh add(Vertex vertex) {
		if(!this.layout().equals(vertex.layout())) {
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
			final Layout layout = DefaultMesh.this.layout();
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
	 * Creates a buffered instance of this mesh.
	 * Note that modifications to this mesh are reflected in the returned buffered mesh.
	 * @return Buffered mesh
	 */
	public BufferedMesh buffer() {
		return new BufferedMesh(this, new VertexBuffer(), null);
	}

	/**
	 * Calculates the bounds of this mesh.
	 * @return Mesh bounds
	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
	 */
	public Bounds bounds() {
		// Determine vertex position from layout
		validate();
		final boolean pos = this.layout().components().stream().anyMatch(e -> e == Point.LAYOUT);
		if(!pos) throw new IllegalStateException("Layout does not contain a vertex position: " + this);

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
	 * @return Triangles indices for this mesh
	 * @throws IllegalStateException if the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	protected Stream<int[]> indices() {
		final Primitive primitive = this.primitive();
		if(!primitive.isTriangle()) throw new IllegalStateException("Mesh does not contain triangular polygons: " + primitive);
		final int faces = primitive.faces(count());
		return IntStream
				.range(0, faces)
				.mapToObj(primitive::indices);
	}
	// TODO - could be parallel stream operation?

	/**
	 * @return Triangles for this mesh
	 * @throws IllegalStateException if the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	public final Stream<Triangle> triangles() {
		return this
				.indices()
				.map(this::triangle)
				.map(Triangle::new);
	}

	/**
	 * Maps the given indices to vertex positions.
	 * @param indices Triangle indices
	 * @return Triangle points
	 */
	private List<Point> triangle(int[] indices) {
		return Arrays
				.stream(indices)
				.mapToObj(vertices::get)
				.map(Vertex::position)
				.toList();
	}

	/**
	 * Computes vertex normals for this mesh.
	 * @throws IllegalStateException if the mesh does not contain vertex data or normals
	 */
	public void compute() {
		// Validate normals can be computed
		final Layout layout = this.layout();
		if(!layout.contains(Point.LAYOUT)) throw new IllegalStateException("Mesh does not contain vertices");
		if(!layout.contains(Normal.LAYOUT)) throw new IllegalStateException("Mesh does not contain vertex normals");
		validate();

		/**
		 * Vertex normals helper.
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
	 * @throws IllegalStateException if this mesh is not valid for rendering
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
