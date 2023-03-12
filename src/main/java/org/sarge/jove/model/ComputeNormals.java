package org.sarge.jove.model;

import static java.util.stream.Collectors.*;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;

/**
 * The <i>compute normals</i> process is a helper class used to compute vertex normals for a given mesh.
 * @author Sarge
 */
class ComputeNormals {
	private final DefaultMesh mesh;

	/**
	 * Constructor.
	 * @param mesh Mesh to compute
	 * @throws IllegalStateException if the drawing primitive of the mesh is not triangular
	 * @see Primitive#isTriangle()
	 */
	public ComputeNormals(DefaultMesh mesh) {
		if(!mesh.primitive().isTriangle()) throw new IllegalStateException("Cannot compute normals for non-triangular primitive: " + mesh);
		this.mesh = notNull(mesh);
	}

	/**
	 * Computes vertex normals.
	 * @return Computed normals indexed by vertex
	 */
	public void compute() {
		final Primitive primitive = mesh.primitive();
		final int count = mesh.count();
		final int faces = primitive.faces(count);
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
			array[n] = mesh.vertex(face + n);
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
}
