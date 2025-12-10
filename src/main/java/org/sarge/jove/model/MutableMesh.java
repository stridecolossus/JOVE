package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.volume.Bounds;

/**
 * A <i>mutable mesh</i> is comprised of {@link Vertex} data.
 * @author Sarge
 */
public class MutableMesh implements Mesh {
	private final Primitive primitive;
	private final List<Layout> layout;
	private final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if {@link #layout} contains a {@link Normal#LAYOUT} but the {@link #primitive} does not support normals
	 * @see Primitive#isTriangle()
	 */
	public MutableMesh(Primitive primitive, Layout... layout) {
		this.primitive = requireNonNull(primitive);
		this.layout = new ArrayList<>(List.of(layout));
		validate();
	}

	private void validate() {
		if(!primitive.isTriangle() && this.layout().contains(Normal.LAYOUT)) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	@Override
	public Primitive primitive() {
		return primitive;
	}

	@Override
	public List<Layout> layout() {
		return Collections.unmodifiableList(layout);
	}

	/**
	 * Looks up the index of the given component layout of this mesh.
	 * @param layout Mesh layout
	 * @return Layout index
	 * @throws IllegalArgumentException if the layout is not a member of this mesh
	 */
	protected int indexOf(Layout component) {
		final int index = this.layout.indexOf(component);
		if(index < 0) {
			throw new IllegalArgumentException("Component layout not present: " + component);
		}
		return index;
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
	 * Removes a vertex component from this mesh and <b>all</i> its vertices.
	 * @param component Component to remove
	 * @throws IllegalArgumentException if {@link #component} is not present
	 */
	public MutableMesh remove(Layout component) {
		// Remove component from layout
		final int index = indexOf(component);
		layout.remove(index);

		// Remove component from vertices
		for(Vertex v : vertices) {
			v.remove(index);
		}

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

	@Override
	public Optional<Index> index() {
		return Optional.empty();
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

	@Override
	public String toString() {
		return String.format("Mesh[primitive=%s layout=%s count=%d]", primitive, layout, count());
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
