package org.sarge.jove.obj;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.texture.TextureCoordinate;
import org.sarge.lib.util.Check;

/**
 * An <i>object model</i> is an intermediate mutable data-set used when loading an OBJ model.
 * @author Sarge
 */
public class ObjectModel {
	/**
	 * A <i>face</i> consist of the indices for a vertex and optionally a normal and texture coordinate.
	 */
	public static final class Face {
		/**
		 * Face indices delimiter.
		 */
		public static final String DELIMITER = "/";

		private final int vertex;
		private final int coords;
		private final int normal;

		/**
		 * Constructor.
		 * @param vertex Vertex index
		 * @param coords Texture coordinates index
		 * @param normal Normal index
		 */
		public Face(int vertex, int coords, int normal) {
			this.vertex = vertex;
			this.coords = coords;
			this.normal = normal;
		}

		/**
		 * @return Vertex index
		 */
		public int vertex() {
			return vertex;
		}

		/**
		 * @return Texture coordinate index
		 */
		public int coords() {
			return coords;
		}

		/**
		 * @return Normal index
		 */
		public int normal() {
			return normal;
		}

		/**
		 * Clamps relative indices to the given group.
		 * @param group Group
		 * @return Clamped face
		 * @throws IllegalArgumentException if the face indices are invalid for the given group
		 */
		private Face clamp(Group group) {
			final int v = clampIndex(vertex, group.vertices);
			final int tc = clampIndex(coords, group.coords);
			final int n = clampIndex(normal, group.normals);
			return new Face(v, tc, n);
		}

		/**
		 * Verifies and clamps a face index.
		 * @param index		Index
		 * @param list		Data size
		 * @return Clamped index
		 */
		private static int clampIndex(int index, List<?> list) {
			// Ignore empty component
			if(index == 0) {
				return 0;
			}

			// Clamp index
			final int size = list.size();
			final int actual;
			if(index < 0) {
				// Clamp relative index
				actual = size + index;
			}
			else {
				// Clamp to zero-indexed
				actual = index - 1;
			}
			assert (actual >= 0) && (actual < size);

			// Verify index
			if(actual >= list.size()) throw new IllegalArgumentException(String.format("Invalid face index: index=%d size=%d", index, size));

			return actual;
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(vertex);
			sb.append(DELIMITER);
			sb.append(coords);
			sb.append(DELIMITER);
			sb.append(normal);
			return sb.toString();
		}
	}

	/**
	 * Group/object.
	 */
	public class Group {
		private final String name;
		private ObjectMaterial mat;
		private final List<Point> vertices = new ArrayList<>();
		private final List<Vector> normals = new ArrayList<>();
		private final List<TextureCoordinate> coords = new ArrayList<>();
		private final List<Face> faces = new ArrayList<>();

		/**
		 * Constructor.
		 * @param name Group/object name
		 */
		private Group(String name) {
			this.name = notEmpty(name);
		}

		/**
		 * @return Group/object name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Material for this group
		 */
		public Optional<ObjectMaterial> material() {
			return Optional.ofNullable(mat);
		}

		/**
		 * Sets the material for this group.
		 * @param mat Material
		 */
		public void material(ObjectMaterial mat) {
			this.mat = mat;
		}

		/**
		 * Adds a vertex to this group.
		 * @param vertex Vertex
		 */
		public void vertex(Point vertex) {
			Check.notNull(vertex);
			vertices.add(vertex);
		}

		/**
		 * Adds a normal to this group.
		 * @param normal Normal
		 */
		public void normal(Vector normal) {
			Check.notNull(normal);
			normals.add(normal);
		}

		/**
		 * Adds a texture coordinate to this group.
		 * @param coords Texture coordinates
		 */
		public void coords(TextureCoordinate coords) {
			Check.notNull(coords);
			this.coords.add(coords);
		}

		/**
		 * Adds a face to this group.
		 * @param face Face
		 * @throws IllegalArgumentException if the given vertex index is zero or the indices are not valid for this group
		 */
		public void face(Face face) {
			if(face.vertex == 0) throw new IllegalArgumentException("Vertex index cannot be zero");
			final Face clamped = face.clamp(this);
			faces.add(clamped);
		}

		/**
		 * Constructs a generic model for this group.
		 * @return Model
		 * @see Model.Builder#build()
		 */
		public Model build() {
			// Create model
			final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLE);
			// TODO - flag for whether to generate extents? or just always do it?

			// Init model components
			final boolean hasCoords = !coords.isEmpty();
			final boolean hasNormals = !normals.isEmpty();
			if(hasCoords) {
				builder.component(Vertex.Component.TEXTURE_COORDINATE);
			}
			if(hasNormals) {
				builder.component(Vertex.Component.NORMAL);
			}

			// Build model vertices
			for(Face face : faces) {
				// Create vertex
				final Point pos = vertices.get(face.vertex);
				final Vertex vertex = new Vertex(pos);

				// Add texture coordinate
				if(hasCoords) {
					vertex.coords(coords.get(face.coords));
				}

				// Add normal
				if(hasNormals) {
					vertex.normal(normals.get(face.normal));
				}

				// Add vertex to model
				builder.add(vertex);
			}

			// Construct model for this group
			return builder.build();
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final Deque<Group> groups = new ArrayDeque<>();

	/**
	 * Constructor.
	 */
	public ObjectModel() {
		groups.add(new Group("root"));
	}

	/**
	 * @return Current group
	 */
	public Group group() {
		return groups.getLast();
	}

	/**
	 * @return Groups in this model
	 */
	public Stream<Group> groups() {
		return groups.stream();
	}

	/**
	 * Starts a new group or object.
	 * @param name Name
	 * @return New group
	 */
	public Group group(String name) {
		// Replace previous group if empty
		final Group current = group();
		if(current.vertices.isEmpty()) {
			groups.removeLast();
		}

		// Create new group
		final Group group = new Group(name);
		groups.add(group);
		return group;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
