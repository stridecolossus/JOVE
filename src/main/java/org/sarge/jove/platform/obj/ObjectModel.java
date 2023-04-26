package org.sarge.jove.platform.obj;

import java.util.*;

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
	private final List<Mesh> meshes = new ArrayList<>();
	private MeshBuilder builder;

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

		// Start new model
		builder = new RemoveDuplicateMeshBuilder(new CompoundLayout(layout));
		meshes.add(builder.mesh());
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
	 * Adds a model vertex.
	 */
	public void add(Vertex vertex) {
		builder.add(vertex);
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Mesh> models() {
		return new ArrayList<>(meshes);
	}
	// TODO - check all groups have same layout
}
