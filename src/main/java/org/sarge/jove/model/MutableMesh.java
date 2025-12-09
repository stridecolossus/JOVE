package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.volume.Bounds;

/**
 * A <i>mutable mesh</i> comprises polygons specified by {@link Vertex}.
 * @author Sarge
 */
public class MutableMesh extends AbstractMesh {
	private final List<Vertex> vertices;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public MutableMesh(Primitive primitive, Layout... layout) {
		this(primitive, List.of(layout), new ArrayList<>());
	}

	/**
	 * Copy constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertex data
	 */
	protected MutableMesh(Primitive primitive, List<Layout> layout, List<Vertex> vertices) {
		super(primitive, layout);
		this.vertices = requireNonNull(vertices);
	}

	@Override
	public int count() {
		return vertices.size();
	}

	/**
	 * Adds a vertex to this mesh.
	 * @param vertex Vertex to add
	 */
	public void add(Vertex vertex) {
		requireNonNull(vertex);
		vertices.add(vertex);
	}

	/**
	 * Removes the vertex component with the given index from the layout of this mesh and <b>all</b> vertices.
	 * @param index Layout index
	 * @return Modified mesh
	 * @throws IndexOutOfBoundsException if {@link #index} is invalid for layout of this mesh
	 */
	public MutableMesh remove(int index) {
		// Remove layout
		final List<Layout> layout = new ArrayList<>(this.layout());
		layout.remove(index);

		// Remove vertex component
		final var copy = new ArrayList<>(this.vertices);
		for(Vertex v : copy) {
			v.remove(index);
		}

		// Create new mesh
		return new MutableMesh(this.primitive(), layout, copy);
	}

	/**
	 * Removes the vertex component with the given layout from this mesh and <b>all</i> vertices.
	 * @param layout Layout to remove
	 * @return Modified mesh
	 * @throws IllegalArgumentException if {@link #layout} is not present
	 */
	public MutableMesh remove(Layout layout) {
		return remove(indexOf(layout));
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
	public MeshData vertices() {
		return new MeshData() {
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
	 * @throws IllegalArgumentException if this mesh does not contain a {@link Point#LAYOUT} component
	 * @see #indexOf(Layout)
	 */
	public Bounds bounds() {
		return bounds(indexOf(Point.LAYOUT));
	}

	/**
	 * Calculates the bounds of this mesh.
	 * @param index Index of the vertex position
	 * @return Mesh bounds
	 */
	public Bounds bounds(int index) {
		if(vertices.isEmpty()) {
			return Bounds.EMPTY;
		}

		return vertices
				.parallelStream()
				.map(vertex -> (Point) vertex.component(index))
				.collect(Bounds.Builder.collector());
	}

	/**
	 *
	 * init list of mutable normals = n vertices
	 *
	 * for each triangle
	 * 		calc normal
	 * 		add normal to each vertex
	 *
	 * normalise
	 *
	 * set normals -> vertices
	 *
	 */

	/**
	 * Computes per-vertex normals for this mesh.
	 * @throws
	 */
	public void compute() {

		// throw if cannot support normals?

		class MutableNormal {
			private float x, y, z;			// TODO - array?

			void add(Normal n) {
				x += n.x;
				y += n.y;
				z += n.z;
			}

			Normal normalise() {
				return new Normal(new Vector(x, y, z));
			}
		}

		final var normals = new MutableNormal[vertices.size()];
		for(int c = 0; c < normals.length; ++c) {
			normals[c] = new MutableNormal();
		}

		final int faces = this.primitive().polygons(this.count());

		for(int face = 0; face < faces; ++face) {

			// map face -> indices then map -> vertices ~ index
			final int[] indices = new int[3];
			for(int n = 0; n < 3; ++n) {
				indices[n] = map(face * 3 + n);			// TODO - different for strip
			}

			final Point[] points = new Point[3];
			for(int n = 0; n < 3; ++n) {
				points[n] = vertices.get(indices[n]).component(0);	// TODO
			}

			final Triangle t = new Triangle(points[0], points[1], points[2]);
			final Normal normal = t.normal();

			for(int c = 0; c < 3; ++c) {
				normals[indices[c]].add(normal);
			}
		}

		for(int c = 0; c < normals.length; ++c) {
			final Normal n = normals[c].normalise();
			//vertices.get(c)
			// TODO - replace
		}
	}


}

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
