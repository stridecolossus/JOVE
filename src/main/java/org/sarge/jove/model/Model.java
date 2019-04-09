package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is a renderable object comprised of mutable vertices.
 * @author Sarge
 */
public class Model<V extends Vertex> {
	private final Primitive primitive;
	private final List<Component> components;
	private final List<V> vertices;
	private final List<Integer> indices;

	/**
	 * Constructor.
	 * @param primitive		Rendering primitive
	 * @param vertices		Vertices
	 * @param extents		Model extents
	 * @throws IllegalArgumentException if the model is empty, the number of vertices is not valid for the rendering primitive, or there is no {@link Component#POSITION} component
	 */
	public Model(Primitive primitive, List<Component> components, List<V> vertices, List<Integer> indices) {
		this.primitive = notNull(primitive);
		this.components = new ArrayList<>(components);
		this.vertices = List.copyOf(vertices);
		this.indices = List.copyOf(indices);
		verify();
	}

	/**
	 * @throws IllegalArgumentException if this model is not valid
	 */
	private void verify() {
		if(vertices.isEmpty()) throw new IllegalArgumentException("Empty model");
		if(!components.contains(Component.POSITION)) throw new IllegalArgumentException("Model must have a vertex position component");
		if(!isIndexed() && !primitive.isValidVertexCount(vertices.size())) throw new IllegalArgumentException("Invalid number of vertices for primitive: " + this);
	}

	/**
	 * @return Rendering primitive
	 */
	public final Primitive primitive() {
		return primitive;
	}

	/**
	 * @return Model components
	 */
	public final List<Vertex.Component> components() {
		return components;
	}

	/**
	 * @return Number of vertices
	 */
	public int length() {
		return vertices.size();
	}

	/**
	 * @return Model vertices
	 */
	public final List<V> vertices() {
		return vertices;
	}

	/**
	 * @return Whether this model is indexed
	 */
	public boolean isIndexed() {
		return !indices.isEmpty();
	}

	/**
	 * @return Model indices
	 */
	public IntStream indices() {
		return indices.stream().mapToInt(Integer::intValue);
	}

	/**
	 * @return Iterator over model triangles
	 * @throws IllegalArgumentException if this model does not consist of triangles
	 */
	public TriangleIterator triangles() {
		if(isIndexed()) {
			final var iterator = indices.stream().map(vertices::get).iterator();
			return new TriangleIterator(iterator);
		}
		else {
			return new TriangleIterator(vertices.iterator());
		}
	}

	/**
	 * Triangle iterator implementation.
	 */
	public class TriangleIterator implements Iterator<Vertex[]> {
		private static final int SIZE = 3;

		private final Iterator<V> iterator;
		private final Vertex[] triangle = new Vertex[SIZE];

		private boolean more = true;

		/**
		 * Constructor.
		 * @param iterator Vertex iterator
		 * @throws IllegalArgumentException if this model does not consist of triangles
		 */
		private TriangleIterator(Iterator<V> iterator) {
			if(primitive.size() != SIZE) throw new IllegalStateException("Not a triangle: " + primitive);
			this.iterator = iterator;
			init();
		}

		/**
		 * Populates the next triangle.
		 */
		private void init() {
			for(int n = 0; n < SIZE; ++n) {
				triangle[n] = iterator.next();
			}
		}

		@Override
		public boolean hasNext() {
			return more;
		}

		@Override
		public Vertex[] next() {
			// Clone next triangle
			if(!more) throw new NoSuchElementException();
			final Vertex[] next = triangle.clone();

			if(iterator.hasNext()) {
				// Populate next triangle
				if(primitive.isStrip()) {
					triangle[0] = triangle[1];
					triangle[1] = triangle[2];
					triangle[2] = iterator.next();
				}
				else {
					init();
				}
			}
			else {
				// Note end of vertices
				more = false;
			}

			return next;
		}
	}

	/**
	 * Generates normals for this model.
	 * @return This model with generated normals
	 * @throws IllegalStateException if this model already has normals or the rendering primitive does not support normals
	 * @throws UnsupportedOperationException if the primitive is not a triangle/strip
	 */
	public Model<V> computeNormals() {
		// Check model supports generated normals
		if(components.contains(Vertex.Component.NORMAL)) throw new IllegalStateException("Model already has normals");
		if(!primitive.hasNormals()) throw new IllegalStateException("Primitive does not support normals: " + primitive);
		if(primitive.size() != 3) throw new UnsupportedOperationException("Only triangle/strip primitives support normal generation");

		// Add normals component
		components.add(Vertex.Component.NORMAL);

		// Initialise normals
		for(V vertex : vertices) {
			vertex.normal(new Vector(0, 0, 0));
		}

		// Generate normals
		final TriangleIterator triangles = triangles();
		boolean even = true;
		while(triangles.hasNext()) {
			// Get next triangle
			final Vertex[] triangle = triangles.next();

			// Build triangle edges
			final Vector ab = edge(triangle[0], triangle[1]);
			final Vector bc = edge(triangle[1], triangle[2]);
			final Vector ac = edge(triangle[0], triangle[2]);

			// Accumulate normals
			add(triangle[0], ab, ac, even);
			add(triangle[1], bc, ab.invert(), even);
			add(triangle[2], ac.invert(), bc.invert(), even);

			// Flip alternate winding order for triangle strips
			if(primitive == Primitive.TRIANGLE_STRIP) {
				even = !even;
			}
		}

		// Normalize
		for(V vertex : vertices) {
			final Vector normal = vertex.normal().normalize();
			vertex.normal(normal);
		}

		return this;
	}

	/**
	 * Builds a triangle edge.
	 * @param start		Start point
	 * @param end		End point
	 * @return Edge
	 */
	private static Vector edge(Vertex start, Vertex end) {
		return Vector.of(start.position(), end.position());
	}

	/**
	 * Accumulates a vertex normal.
	 * @param vertex	Vertex
	 * @param u			Left-hand edge
	 * @param v			Right-hand edge
	 * @param invert	Whether to invert the normal
	 * @see MutableNormalVertex
	 */
	private static void add(Vertex vertex, Vector u, Vector v, boolean even) {
		final Vector normal = u.cross(v);
		final Vector actual = even ? normal : normal.invert();
		final Vector result = vertex.normal().add(actual);
		vertex.normal(result);
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this)
			.setExcludeFieldNames("vertices", "indices")
			.append("vertices", vertices.size())
			.append("indices", indices.size())
			.toString();
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder<V extends Vertex> {
		private Primitive primitive = Primitive.TRIANGLE_LIST;
		private final List<Vertex.Component> components = new StrictList<>();
		private final List<V> vertices = new ArrayList<>();
		private final List<Integer> indices = new ArrayList<>();
		private final Extents.Builder extents = new Extents.Builder();

		/**
		 * Constructor.
		 */
		public Builder() {
			component(Vertex.Component.POSITION);
			verify();
		}

		/**
		 * Sets the rendering primitive (default is {@link Primitive#TRIANGLE}).
		 * @param primitive Rendering primitive
		 * @throws IllegalStateException if this model configuration is not valid
		 */
		public Builder<V> primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			verify();
			return this;
		}

		/**
		 * Adds a component to this model.
		 * @param c Component
		 * @throws IllegalStateException if this model configuration is not valid
		 * @throws IllegalArgumentException for a duplicate component
		 */
		public Builder<V> component(Vertex.Component c) {
			components.add(c);
			verify();
			return this;
		}

		/**
		 * @throws IllegalStateException if this model is not valid
		 */
		private void verify() {
			// Check model can be configured
			if(!vertices.isEmpty()) {
				throw new IllegalStateException("Cannot configure a partially constructed model");
			}

			// Check normals are supported
			if(components.contains(Vertex.Component.NORMAL) && !primitive.hasNormals()) {
				throw new IllegalStateException("Primitive does not support normals: " + primitive);
			}
		}

		/**
		 * Adds a vertex to this model.
		 * @param vertex Vertex to add
		 */
		public Builder<V> add(V vertex) {
			vertices.add(vertex);
			extents.add(vertex.position());
			return this;
		}

		/**
		 * Adds a vertex index to this model.
		 * @param index Index
		 * @throws IllegalArgumentException if the index is negative or exceeds the number of vertices
		 */
		public Builder<V> add(int index) {
			Check.zeroOrMore(index);
			if(index >= vertices.size()) throw new IllegalArgumentException("Invalid index for this model: " + index);
			indices.add(index);
			return this;
		}

		/**
		 * Constructs this model.
		 * @return New model
		 */
		public Model<V> build() {
			return new Model<>(primitive, components, vertices, indices);
		}

		/**
		 * @return Model extents
		 */
		public Extents extents() {
			return extents.build();
		}
	}
}
