package org.sarge.jove.model;

import static java.util.stream.Collectors.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.volume.Bounds;
import org.sarge.lib.util.Check;

/**
 * A <i>mesh builder</i> is used to construct a renderable {@link Mesh}.
 * <p>
 * Vertex normals can be automatically computed using the {@link #compute()} method.
 * <p>
 * @see IndexedMeshBuilder
 * @author Sarge
 */
public class MeshBuilder {
	private final List<Vertex> vertices = new ArrayList<>();
	private final Mesh mesh;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @see Mesh
	 */
	public MeshBuilder(Primitive primitive, CompoundLayout layout) {
		this.mesh = new Mesh(primitive, layout, () -> count(), new VertexBuffer(), index());
	}

	/**
	 * Adds a vertex to this mesh.
	 * @param vertex Vertex to add
	 */
	public MeshBuilder add(Vertex vertex) {
		Check.notNull(vertex);
		vertices.add(vertex);
		return this;
	}

	/**
	 * @return Mesh
	 */
	public Mesh mesh() {
		return mesh;
	}

	/**
	 * @return Draw count
	 */
	protected int count() {
		return vertices.size();
	}

	/**
	 * Retrieves a vertex.
	 * @param index Vertex index
	 * @return Vertex
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public Vertex vertex(int index) {
		return vertices.get(index);
	}

	/**
	 * Mesh vertices as an NIO vertex buffer.
	 */
	private class VertexBuffer implements ByteSizedBufferable {
		@Override
		public int length() {
			return vertices.size() * mesh.layout().stride();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			for(Vertex v : vertices) {
				v.buffer(bb);
			}
		}
	}

	/**
	 * @return Index buffer
	 */
	protected ByteSizedBufferable index() {
		return null;
	}

	/**
	 * Calculates the bounds of this mesh.
	 * @return Mesh bounds
	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
	 * @throws IllegalStateException if {@link #count()} is not valid for the drawing primitive
	 */
	public final Bounds bounds() {
		mesh.validate();
		return vertices
				.parallelStream()
				.map(Vertex::position)
				.collect(Bounds.Builder.collector())
				.build();
	}

	/**
	 * Computes per-vertex normals for this mesh.
	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
	 * @throws IllegalStateException if {@link #count()} is not valid for the drawing primitive
	 */
	public void compute() {
		// Check mesh can be rendered
		mesh.validate();

		// Determine number of triangles
		final int faces = mesh.primitive().faces(this.count());

		// Compute normals
		IntStream
				.range(0, faces)
				.mapToObj(this::vertices)
				.map(Arrays::asList)
				.flatMap(this::normals)
				.collect(groupingBy(VertexNormal::vertex, mapping(VertexNormal::normal, toList())))
				.forEach(VertexNormal::normalize);
	}

	/**
	 * Builds the triangle vertices for the given face.
	 * @param face Triangle index
	 * @return Triangle vertices
	 */
	private Vertex[] vertices(int face) {
		final Vertex[] array = new Vertex[3];
		for(int n = 0; n < array.length; ++n) {
			array[n] = vertex(face + n);
		}
		return array;
	}

	/**
	 * Transient vertex normal.
	 */
	private record VertexNormal(Vertex vertex, Vector normal) {
		/**
		 * Populates the normal for each vertex.
		 * @param vertex		Vertex to update
		 * @param normals		Vertex normals
		 */
		private static void normalize(Vertex vertex, List<Vector> normals) {
			for(Vector n : normals) {
				vertex.add(n);
			}
		}
	}

	/**
	 * Computes the normal for the triangle vertices.
	 * @param vertices Triangle vertices
	 * @return Vertex normals
	 */
	private Stream<VertexNormal> normals(List<Vertex> vertices) {
		final List<Point> points = vertices.stream().map(Vertex::position).toList();
		final Triangle triangle = new Triangle(points);
		final Vector normal = triangle.normal();
		return vertices.stream().map(v -> new VertexNormal(v, normal));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(mesh).build();
	}
}
