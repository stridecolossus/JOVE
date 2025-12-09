package org.sarge.jove.platform.obj;

import java.util.*;

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
	private final List<IndexedMesh> meshes = new ArrayList<>();
	private IndexedMesh current;

	public ObjectModel() {
		append();
	}

	/**
	 * Starts a new mesh.
	 */
	private void append() {
		current = new RemoveDuplicateMesh();
		meshes.add(current);
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

//	/**
//	 * @return Layout for the current model
//	 */
//	private List<Layout> layout() {
//		final var layout = new ArrayList<Layout>();
//		layout.add(Point.LAYOUT);
//		if(!normals.isEmpty()) {
//			layout.add(Normal.LAYOUT);
//		}
//		if(!coords.isEmpty()) {
//			layout.add(Coordinate2D.LAYOUT);
//		}
//		return layout;
//	}

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore empty models
		if(current.count() == 0) {
			return;
		}

		// Construct the current model
		append();

		// Initialise model
		positions.clear();
		normals.clear();
		coords.clear();
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
