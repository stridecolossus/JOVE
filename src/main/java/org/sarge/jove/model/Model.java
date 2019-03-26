package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.MutableNormalVertex;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.lib.collection.StrictList;

/**
 * A <i>model</i> is a renderable object comprised of mutable vertices.
 * @author Sarge
 */
public class Model<V extends MutableNormalVertex> {
	private final Primitive primitive;
	private final List<Component> components;
	private final List<V> vertices;
	private final Extents extents;

	/**
	 * Constructor.
	 * @param primitive		Rendering primitive
	 * @param vertices		Vertices
	 * @param extents		Model extents
	 * @throws IllegalArgumentException if the model is empty, the number of vertices is not valid for the rendering primitive, or there is no {@link Component#POSITION} component
	 */
	public Model(Primitive primitive, List<Component> components, List<V> vertices, Extents extents) {
		this.primitive = notNull(primitive);
		this.components = new ArrayList<>(components);
		this.vertices = List.copyOf(vertices);
		this.extents = notNull(extents);
		verify();
	}

	/**
	 * Copy constructor.
	 * @param model Model to copy
	 * @see #Model(Primitive, List, List, Extents)
	 */
	protected Model(Model<V> model) {
		this(model.primitive, model.components, model.vertices, model.extents);
	}

	/**
	 * @throws IllegalArgumentException if this model is not valid
	 */
	private void verify() {
		if(vertices.isEmpty()) throw new IllegalArgumentException("Empty model");
		if(!components.contains(Component.POSITION)) throw new IllegalArgumentException("Model requires a vertex position component");
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
	 * @return Whether this model is indexed
	 */
	public boolean isIndexed() {
		return false;
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
	 * @return Extents of this model
	 * @see Component#EXTENTS
	 */
	public final Extents extents() {
		return extents;
	}

	/**
	 * @return Iterator over model faces
	 */
	public Iterator<List<V>> faces() {
		return new FaceIterator(vertices.iterator());
	}

	/**
	 * Face iterator implementation.
	 */
	protected class FaceIterator implements Iterator<List<V>> {
		private final int size = primitive.size();
		private final Iterator<V> iterator;
		private final List<V> face = new ArrayList<>(size);

		private boolean more = true;

		/**
		 * Constructor.
		 * @param iterator Face vertex iterator
		 */
		protected FaceIterator(Iterator<V> iterator) {
			this.iterator = iterator;
			init();
		}

		/**
		 * Initialises the first face.
		 */
		private void init() {
			for(int n = 0; n < size; ++n) {
				face.add(iterator.next());
			}
		}

		@Override
		public boolean hasNext() {
			return more;
		}

		@Override
		public List<V> next() {
			// Clone face
			if(!more) throw new NoSuchElementException();
			final var<V> next = List.copyOf(face);

			if(iterator.hasNext()) {
				// Init next face
				if(primitive.isStrip()) {
					// Shift strip face by one and append next vertex
					Collections.rotate(face, -1);
					face.set(size - 1, iterator.next());
				}
				else {
					// Populate non-strip face
					for(int n = 0; n < size; ++n) {
						face.set(n, iterator.next());
					}
				}
			}
			else {
				// Note end of faces
				more = false;
			}

			return next;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	// TODO - this should be moved to builder?

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

		// Generate normals
		// TODO - faces() could return actual Face class with the following logic
		final var faces = faces();
		boolean even = true;
		while(faces.hasNext()) {
			// Lookup triangle vertices
			final var triangle = faces.next();
			final V a = triangle.get(0);
			final V b = triangle.get(1);
			final V c = triangle.get(2);

			// Build triangle edges
			final Vector ab = edge(a, b);
			final Vector bc = edge(b, c);
			final Vector ac = edge(a, c);

			// Accumulate normals
			add(a, ab, ac, even);
			add(b, bc, ab.invert(), even);
			add(c, ac.invert(), bc.invert(), even);

			// Invert normals
			// TODO - only valid for triangle-strip
			even = !even;
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
	private Vector edge(V start, V end) {
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
	private void add(V vertex, Vector u, Vector v, boolean even) {
		final Vector normal = u.cross(v);
		final Vector actual = even ? normal : normal.invert();
		final Vector result = vertex.normal().add(actual);
		vertex.normal(result);
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this)
			.setExcludeFieldNames("vertices", "indices")
			.append("indexed", isIndexed())
			.append("vertices", vertices.size())
			.append("size", length())
			.toString();
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder<V extends MutableVertex> {
		private Primitive primitive = Primitive.TRIANGLE;
		private final List<Vertex.Component> components = new StrictList<>();
		private final List<V> vertices = new ArrayList<>();
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
		 * Constructs this model.
		 * @return New model
		 */
		public Model<V> build() {
			return new Model<>(primitive, components, vertices, extents.build());
		}
	}
}
