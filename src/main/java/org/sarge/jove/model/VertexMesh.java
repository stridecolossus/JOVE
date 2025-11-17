package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.scene.volume.Bounds;

/**
 * A <i>mutable mesh</i> comprises polygons specified by {@link Vertex}.
 * <p>
 * Vertex normals can be automatically computed using the {@link #compute()} method.
 * <p>
 * @see IndexedVertexMesh
 * @author Sarge
 */
public class VertexMesh extends AbstractMesh {
	private final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @see Mesh
	 */
	public VertexMesh(Primitive primitive, List<Layout> layout) {
		super(primitive, layout);
	}

	@Override
	public int count() {
		return vertices.size();
	}

	/**
	 * Adds a vertex to this mesh.
	 * @param vertex Vertex to add
	 */
	public VertexMesh add(Vertex vertex) {
		requireNonNull(vertex);
		vertices.add(vertex);
		return this;
	}

	/**
	 * Maps a vertex index.
	 * @param vertex Vertex index
	 * @return Actual vertex index
	 */
	protected int map(int vertex) {
		return vertex;
	}

	@Override
	public DataBuffer vertices() {
		return new DataBuffer() {
			@Override
			public int length() {
				return vertices.size() * Layout.stride(layout());
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Vertex v : vertices) {
					v.buffer(buffer);
				}
			}
		};
	}

	/**
	 * Calculates the bounds of this mesh.
	 * @return Mesh bounds
	 */
	public Bounds bounds() {
		if(vertices.isEmpty()) {
			return Bounds.EMPTY;
		}

		return vertices
				.parallelStream()
				.map(Vertex::position)
				.collect(Bounds.Builder.collector());
	}

	/**
	 * Computes per-vertex normals for this mesh.
	 */
	public void compute() {
		//final int polygons = this.primitive().polygons(this.count());

//		IntStream
//				.range(0, polygons)
//				.mapToObj(this::polygon)

		// init accumulated normals
		// map each polygon -> vertices (indices)
		// calculate face normal
		// add to normals for each of vertices
		// normalize all
		// update to mesh

	}

	private List<Vertex> polygon(int index) {
		return null;
	}
}

//
//	/**
//	 * Computes per-vertex normals for this mesh.
//	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
//	 * @throws IllegalStateException if {@link #count()} is not valid for the drawing primitive
//	 */
//	public void compute() {
//		// Check mesh can be rendered
//		mesh.validate();
//
//		// Determine number of triangles
//		final int faces = mesh.primitive().faces(this.count());
//
//		// Compute normals
//		IntStream
//				.range(0, faces)
//				.mapToObj(this::vertices)
//				.map(Arrays::asList)
//				.flatMap(this::normals)
//				.collect(groupingBy(VertexNormal::vertex, mapping(VertexNormal::normal, toList())))
//				.forEach(VertexNormal::normalize);
//	}
//
//	/**
//	 * Builds the triangle vertices for the given face.
//	 * @param face Triangle index
//	 * @return Triangle vertices
//	 */
//	private Vertex[] vertices(int face) {
//		final Vertex[] array = new Vertex[3];
//		for(int n = 0; n < array.length; ++n) {
//			array[n] = vertex(face + n);
//		}
//		return array;
//	}
//
////	/**
////	 * Transient vertex normal.
////	 */
////	private record VertexNormal(Vertex vertex, Vector normal) {
////		/**
////		 * Populates the normal for each vertex.
////		 * @param vertex		Vertex to update
////		 * @param normals		Vertex normals
////		 */
////		private static void normalize(Vertex vertex, List<Vector> normals) {
////			for(Vector n : normals) {
////				vertex.add(n);
////			}
////		}
////	}
//
//	/**
//	 * Computes the normal for the triangle vertices.
//	 * @param vertices Triangle vertices
//	 * @return Vertex normals
//	 */
//	private Stream<VertexNormal> normals(List<Vertex> vertices) {
//		final List<Point> points = vertices.stream().map(Vertex::position).toList();
//		final Triangle triangle = new Triangle(points);
//		final Vector normal = triangle.normal();
//		return vertices.stream().map(v -> new VertexNormal(v, normal));
//	}
//}
