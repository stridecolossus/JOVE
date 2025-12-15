package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.common.Layout;
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
	private final VertexComponentList<Coordinate2D> coordinates = new VertexComponentList<>();
	private final List<IndexedMesh> meshes = new ArrayList<>();
	private IndexedMesh current;

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
		return coordinates;
	}

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore leading group commands
		if(positions.isEmpty()) {
			return;
		}

		// Determine model layout
		final var layout = new ArrayList<>();
		layout.add(Point.LAYOUT);
		if(!normals.isEmpty()) {
			layout.add(Normal.LAYOUT);
		}
		if(!coordinates().isEmpty()) {
			layout.add(Coordinate2D.LAYOUT);
		}

		// Start model
		current = new RemoveDuplicateMesh(layout.toArray(Layout[]::new));
		meshes.add(current);
	}

	/**
	 * Adds a model vertex.
	 */
	public void add(Vertex vertex) {
		current.add(vertex);
	}

	/**
	 * Constructs this OBJ model.
	 * @return Meshes
	 */
	public List<IndexedMesh> build() {
		return meshes;
	}
}
