package org.sarge.jove.platform.obj;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * The <i>OBJ model</i> comprises the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final VertexComponentList<Point> positions = new VertexComponentList<>();
	private final VertexComponentList<Normal> normals = new VertexComponentList<>();
	private final VertexComponentList<Coordinate2D> coords = new VertexComponentList<>();
	private final List<DefaultMesh> models = new ArrayList<>();
	private DefaultMesh model;
	private int components;

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if current group is empty
		if(positions.isEmpty()) {
			return;
		}

		// Build current model group
		build();

//		// Reset transient model
//		positions.clear();
//		normals.clear();
//		coords.clear();
	}

	/**
	 * Constructs the current object.
	 */
	private void build() {
		// Init model layout
		final var layout = new ArrayList<Layout>();
		layout.add(Point.LAYOUT);
		if(!normals.isEmpty()) {
			layout.add(Normal.LAYOUT);
		}
		if(!coords.isEmpty()) {
			layout.add(Coordinate2D.LAYOUT);
		}
		components = layout.size();

		// Start new model
		model = new RemoveDuplicateMesh(new CompoundLayout(layout));
		models.add(model);
	}

	/**
	 * @return Vertex positions
	 */
	public VertexComponentList<Point> positions() {
		return positions;
	}

	/**
	 * @return Normals
	 */
	public VertexComponentList<Normal> normals() {
		return normals;
	}

	/**
	 * @return Texture coordinates
	 */
	public VertexComponentList<Coordinate2D> coordinates() {
		return coords;
	}

	/**
	 * @return Number of vertex components per face
	 */
	public int components() {
		return components;
	}

	/**
	 * Custom OBJ vertex with optional vertex normal and texture coordinate.
	 */
	private static class MutableVertex extends Vertex {
		private Normal normal;
		private Coordinate2D coord;

		/**
		 * Constructor.
		 * @param pos Vertex position
		 */
		public MutableVertex(Point pos) {
			super(pos);
		}

		/**
		 * Sets the vertex normal.
		 */
		void normal(Normal normal) {
			this.normal = normal;
		}

		/**
		 * Sets the texture coordinate.
		 */
		void coordinate(Coordinate2D coord) {
			this.coord = coord;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			super.buffer(bb);
			if(normal != null) {
				normal.buffer(bb);
			}
			if(coord != null) {
				coord.buffer(bb);
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.position(), normal, coord);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof MutableVertex that) &&
					this.position().equals(that.position()) &&
					Objects.equals(this.normal, that.normal) &&
					Objects.equals(this.coord, that.coord);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("normal", normal)
					.append("coord", coord)
					.build();
		}
	}

	/**
	 * Adds a vertex to the current model.
	 * <ul>
	 * <li>The vertex position is mandatory</li>
	 * <li>Texture coordinates and normals are optional, i.e. can be {@code null}</li>
	 * <li>Indices start at <b>one</b> and can be negative</li>
	 * </ul>
	 * @param indices Vertex component indices
	 * @throws IndexOutOfBoundsException for an invalid index
	 * @see VertexComponentList#get(int)
	 */
	public void vertex(int[] indices) {
		// Create vertex
		final Point pos = positions.get(indices[0]);
		final var vertex = new MutableVertex(pos);

		// Add optional texture coordinate
		if(indices[1] != 0) {
			final Coordinate2D coord = coords.get(indices[1]);
			vertex.coordinate(coord);
		}

		// Add optional vertex normal
		if(indices[2] != 0) {
			final Normal normal = normals.get(indices[2]);
			vertex.normal(normal);
		}

		// Add to model
		model.add(vertex);
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Mesh> models() {
//		build();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
